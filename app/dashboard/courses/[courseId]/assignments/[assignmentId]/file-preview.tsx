import React, { useEffect, useState, useRef } from "react";
import { Title, Text, Button, Stack, Loader, Group } from "@mantine/core";
import * as PDFJS from "pdfjs-dist";

// Configure PDF.js worker
PDFJS.GlobalWorkerOptions.workerSrc = "/pdf.worker.min.mjs";

interface FileData {
  id: string;
  originalName: string;
  content?: string;
  ocrResult?: string;
  totalPages?: number;
}

interface FilePreviewProps {
  fileIds: string[];
  onOcrRequest?: (fileId: string) => Promise<string>;
}

export function FilePreview({ fileIds, onOcrRequest }: FilePreviewProps) {
  const [files, setFiles] = useState<FileData[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [currentPages, setCurrentPages] = useState<{ [key: string]: number }>(
    {},
  );
  const [previewLoaded, setPreviewLoaded] = useState<{
    [key: string]: boolean;
  }>({});
  const canvasRefs = useRef<{ [key: string]: HTMLCanvasElement | null }>({});

  useEffect(() => {
    if (fileIds.length === 0) return;

    setLoading(true);
    const existingIds = files.map((f) => f.id);
    const newIds = [
      ...new Set(fileIds.filter((id) => !existingIds.includes(id))),
    ];

    const fetchPromises = newIds.map((fileId) => {
      return fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/files/${fileId}?metadata=true`,
        {
          credentials: "include",
        },
      )
        .then((metadataResponse) => {
          if (!metadataResponse.ok) {
            throw new Error("Failed to fetch file metadata");
          }
          return metadataResponse.json();
        })
        .then(async (metadata) => {
          const fileData: FileData = {
            id: fileId,
            originalName: metadata.data.originalName,
          };

          const name = metadata.data.originalName.toLowerCase();

          if (name.endsWith(".md")) {
            const contentResponse = await fetch(
              `${process.env.NEXT_PUBLIC_API_URL}/files/${fileId}`,
              { credentials: "include" },
            );
            fileData.content = await contentResponse.text();
          } else {
            const response = await fetch(
              `${process.env.NEXT_PUBLIC_API_URL}/files/${fileId}/pdf`,
              { credentials: "include" },
            );
            if (!response.ok) throw new Error("Failed to fetch PDF file");
            const pdfData = await response.arrayBuffer();
            const pdf = await PDFJS.getDocument({ data: pdfData }).promise;
            fileData.totalPages = pdf.numPages;
          }

          // 即使没有 md/pdf 内容，也返回 fileData
          return fileData;
        });
    });

    Promise.all(fetchPromises)
      .then((newFiles) => {
        setFiles((prev) => {
          const uniqueNewFiles = newFiles.filter(
            (nf) => !prev.some((f) => f.id === nf.id),
          );
          return [...prev, ...uniqueNewFiles];
        });
        setCurrentPages((prev) => {
          const updated = { ...prev };
          newFiles.forEach((file) => {
            if (file.totalPages) updated[file.id] = 1;
          });
          return updated;
        });
        setPreviewLoaded((prev) => {
          const updated = { ...prev };
          newFiles.forEach((file) => {
            updated[file.id] = false;
          });
          return updated;
        });
      })
      .catch((err) => {
        setError(err.message);
      })
      .finally(() => {
        setLoading(false);
      });
  }, [fileIds]);

  const renderPDF = async (
    fileId: string,
    canvas: HTMLCanvasElement,
    pageNumber: number,
  ) => {
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/files/${fileId}/pdf`,
        { credentials: "include" },
      );
      if (!response.ok) throw new Error("Failed to fetch PDF file");

      const pdfData = await response.arrayBuffer();
      const pdf = await PDFJS.getDocument({ data: pdfData }).promise;
      const page = await pdf.getPage(pageNumber);

      const viewport = page.getViewport({ scale: 1.5 });
      canvas.height = viewport.height;
      canvas.width = viewport.width;

      const renderContext = {
        canvasContext: canvas.getContext("2d")!,
        viewport,
      };
      await page.render(renderContext).promise;
      setPreviewLoaded((prev) => ({ ...prev, [fileId]: true }));
    } catch (err: any) {
      setError("This file format does not support preview");
    }
  };

  const handleOcrRequest = async (fileId: string) => {
    if (onOcrRequest) {
      try {
        const result = await onOcrRequest(fileId);
        setFiles((prev) =>
          prev.map((file) =>
            file.id === fileId ? { ...file, ocrResult: result } : file,
          ),
        );
      } catch (err: any) {
        setError("Failed to fetch OCR result: " + err.message);
      }
    }
  };

  const handlePageChange = (
    fileId: string,
    page: number,
    totalPages?: number,
  ) => {
    if (totalPages && (page < 1 || page > totalPages)) return;
    setCurrentPages((prev) => ({ ...prev, [fileId]: page }));
    const canvas = canvasRefs.current[fileId];
    if (canvas) renderPDF(fileId, canvas, page);
  };

  if (loading) return <Loader aria-label="Loading file preview" />;
  if (error)
    return (
      <Text c="red" aria-label="Error message">
        Error: {error}
      </Text>
    );

  return (
    <Stack p="md" aria-label="File preview stack">
      <Title order={2} aria-label="File preview title">
        File Preview ({files.length})
      </Title>

      {files.map((file) => (
        <div
          key={file.id}
          style={{
            borderBottom: "1px solid #eee",
            paddingBottom: "1rem",
            marginBottom: "1rem",
          }}
          aria-label={`File preview container: ${file.originalName}`}
        >
          <Text aria-label="Filename text">
            <strong>Filename:</strong> {file.originalName} {file.totalPages}
          </Text>

          {onOcrRequest && (
            <Button
              onClick={() => handleOcrRequest(file.id)}
              mb="md"
              aria-label="View OCR result button"
            >
              View OCR Result
            </Button>
          )}

          {file.ocrResult && (
            <Text aria-label="OCR result text">
              <strong>OCR Result:</strong> {file.ocrResult}
            </Text>
          )}

          {file.originalName.endsWith(".md") && file.content ? (
            <pre
              style={{
                background: "#f5f5f5",
                padding: "1rem",
                borderRadius: "4px",
                overflowX: "auto",
              }}
              aria-label="Markdown content"
            >
              {file.content}
            </pre>
          ) : /\.(docx|doc|pptx|ppt|xlsx|xls|txt|jpg|jpeg|png|gif|pdf)$/i.test(
              file.originalName,
            ) ? (
            <>
              <canvas
                ref={(el) => {
                  canvasRefs.current[file.id] = el;
                }}
                style={{ maxWidth: "100%", height: "auto" }}
                aria-label="PDF preview canvas"
              />
              {(file.originalName.toLowerCase().endsWith(".docx") ||
                file.originalName.toLowerCase().endsWith(".doc") ||
                file.originalName.toLowerCase().endsWith(".pptx") ||
                file.originalName.toLowerCase().endsWith(".ppt") ||
                file.originalName.toLowerCase().endsWith(".xlsx") ||
                file.originalName.toLowerCase().endsWith(".xls") ||
                file.originalName.toLowerCase().endsWith(".txt") ||
                file.originalName.toLowerCase().endsWith(".jpg") ||
                file.originalName.toLowerCase().endsWith(".jpeg") ||
                file.originalName.toLowerCase().endsWith(".png") ||
                file.originalName.toLowerCase().endsWith(".gif") ||
                file.originalName.toLowerCase().endsWith(".pdf")) &&
                previewLoaded[file.id] &&
                file.totalPages && (
                  <Group mt="sm" aria-label="PDF navigation group">
                    <Button
                      disabled={currentPages[file.id] === 1}
                      onClick={() =>
                        handlePageChange(
                          file.id,
                          (currentPages[file.id] || 1) - 1,
                          file.totalPages,
                        )
                      }
                      aria-label="Previous page button"
                    >
                      Previous Page
                    </Button>
                    <Text aria-label="Page info text">
                      Page {currentPages[file.id] || 1} / {file.totalPages}
                    </Text>
                    <Button
                      disabled={currentPages[file.id] === file.totalPages}
                      onClick={() =>
                        handlePageChange(
                          file.id,
                          (currentPages[file.id] || 1) + 1,
                          file.totalPages,
                        )
                      }
                      aria-label="Next page button"
                    >
                      Next Page
                    </Button>
                  </Group>
                )}
              <Button
                onClick={() => {
                  const canvas = canvasRefs.current[file.id];
                  if (canvas)
                    renderPDF(file.id, canvas, currentPages[file.id] || 1);
                }}
                mt="sm"
                aria-label="Load PDF preview button"
              >
                Load PDF Preview
              </Button>
            </>
          ) : (
            <div
              style={{ color: "gray", fontStyle: "italic" }}
              aria-label="Unsupported file format label"
            >
              This file format does not support preview.
            </div>
          )}

          <Button
            component="a"
            href={`${process.env.NEXT_PUBLIC_API_URL}/files/${file.id}`}
            download
            ml="sm"
            mt="sm"
            aria-label="Download original file button"
          >
            Download Original File
          </Button>
        </div>
      ))}
    </Stack>
  );
}
