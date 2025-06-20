/*
 * 显示作业文件列表
 */

// AI-generated-content
// tool: Deepseek-R1
// version: latest
// usage: directly use and manual debug


"use client";

import React, { useEffect, useState } from "react";
import {
  Anchor,
  Stack,
  Table,
  Text,
  Title,
  Button,
  Modal,
} from "@mantine/core";
import Link from "next/link";
import { FilePreview } from "./file-preview";

export function AssignmentFiles(props: { assignmentId: number }) {
  const [files, setFiles] = useState<any[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [previewFileId, setPreviewFileId] = useState<string | null>(null);

  useEffect(() => {
    const fetchFiles = async () => {
      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/assignment-files?assignment-id=${props.assignmentId}`,
          {
            credentials: "include",
          },
        );

        if (!response.ok) {
          throw new Error("Failed to fetch assignment files");
        }

        const data = await response.json();
        setFiles(data.data);
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchFiles();
  }, [props.assignmentId]);

  const handlePreview = (fileId: string) => {
    setPreviewFileId(fileId);
  };

  if (loading) {
    return <div aria-label="Loading assignment files">Loading...</div>;
  }
  if (error) {
    return <div aria-label="Error message">Error: {error}</div>;
  }
  if (!files || files.length === 0) {
    return (
      <Text
        style={{
          color: "gray",
          fontSize: 14,
        }}
        aria-label="No files label"
      >
        No files for this assignment.
      </Text>
    );
  } else {
    return (
      <Stack
        bg="var(--mantine-color-body)"
        align="flex-start"
        justify="flex-start"
        gap="sm"
        style={{
          paddingTop: 8,
          paddingLeft: 0,
        }}
        aria-label="Assignment files stack"
      >
        <Title order={2} aria-label="Assignment files title">
          Assignment files
        </Title>
        <Table highlightOnHover aria-label="Assignment files table">
          <Table.Thead aria-label="Table header">
            <Table.Tr>
              <Table.Th aria-label="File name column">File Name</Table.Th>
              <Table.Th aria-label="Action column">Action</Table.Th>
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody aria-label="Table body">
            {files.map((assignmentFile) => (
              <Table.Tr
                key={assignmentFile.id}
                aria-label={`File row: ${assignmentFile.file.originalName}`}
              >
                <Table.Td aria-label="File name cell">
                  {assignmentFile.file.originalName}
                </Table.Td>
                <Table.Td aria-label="Action cell">
                  <Button
                    variant="subtle"
                    size="xs"
                    onClick={() => handlePreview(assignmentFile.file.id)}
                    aria-label={`Preview button for file ${assignmentFile.file.originalName}`}
                  >
                    Preview
                  </Button>
                </Table.Td>
              </Table.Tr>
            ))}
          </Table.Tbody>
        </Table>
        <Modal
          opened={!!previewFileId}
          onClose={() => setPreviewFileId(null)}
          title="File Preview"
          size="lg"
          aria-label="File preview modal"
        >
          {previewFileId && (
            <FilePreview
              fileIds={[previewFileId]}
              aria-label="File preview component"
            />
          )}
        </Modal>
      </Stack>
    );
  }
}
