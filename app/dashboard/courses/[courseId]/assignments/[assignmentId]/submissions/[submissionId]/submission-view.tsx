// AI-generated-content
// tool: Deepseek-R1
// version: latest
// usage: directly use and manual debug
"use client";
import React, { MouseEventHandler, useEffect, useState } from "react";
import {
  Text,
  Loader,
  Alert,
  Paper,
  Group,
  Stack,
  Card,
  NavLink,
  Box,
  Divider,
  useMantineColorScheme,
  Collapse,
  Button,
  List,
  Modal,
  LoadingOverlay,
  Notification,
} from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { Ellipsis, ChevronDown, ChevronUp } from "lucide-react";
import { ScoreCard } from "../../score-card";
import { FilePreview } from "../../file-preview";

export const SubmissionView = (props: {
  courseId: number;
  assignmentId: number;
  submissionId: number;
  role: string;
}) => {
  const data = [
    {
      stdout: "hello, Judge0\n",
      time: "0.001",
      memory: 376,
      stderr: null,
      token: "8531f293-1585-4d36-a34c-73726792e6c9",
      compile_output: null,
      message: null,
      status: {
        id: 6,
        description: "Accepted",
      },
    },
    {
      stdout: "hello, Judge0\n",
      time: "0.003",
      memory: 376,
      stderr: null,
      token: "8531f293-1585-4d36-a34c-73726792sbg5",
      compile_output: null,
      message: null,
      status: {
        id: 13,
        description: "Accepted",
      },
    },
  ];
  {
    /* 本次提交数据以及测试样例信息 */
  }
  const [assignment, setAssignment] = useState<any>(null);
  const [submission, setSubmission] = useState<any>(null);
  const [submissionFiles, setSubmissionFiles] = useState<any[]>([]);
  const [submissionJudges, setSubmissionJudges] = useState<any[]>([]);
  {
    /* 文件是否被点击 */
  }
  const [active, setActive] = useState(0);
  {
    /* 扩展结果 */
  }
  const [expandedJudge, setExpandedJudge] = useState<string | null>(null);
  {
    /* 计算分数 */
  }
  const [calculatedScore, setCalculatedScore] = useState(0);
  const [calculatedFullMark, setCalculatedFullMark] = useState(0);
  const [pending, setPending] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [previewFileId, setPreviewFileId] = useState<string | null>(null);
  const { colorScheme } = useMantineColorScheme();

  const handleOcrRequest = async (fileId: string) => {
    console.log("[OCR] handleOcrRequest called with fileId:", fileId);
    setLoading(true);
    setError(null);
    try {
      // 等待OCR处理（假设后端上传后自动触发）
      await new Promise((resolve) => setTimeout(resolve, 2000));

      // 下载OCR处理后的PDF文件
      const downloadResponse = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/files/${fileId}/ocr`,
        {
          credentials: "include",
        },
      );
      if (!downloadResponse.ok) {
        const errorMessage =
          downloadResponse.status === 404
            ? "File not found or OCR not processed"
            : downloadResponse.statusText ||
            "Failed to download OCR-processed file";
        throw new Error(errorMessage);
      }

      // 处理PDF二进制响应
      const blob = await downloadResponse.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `${fileId}.pdf`; // 与后端文件名一致
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);

      return "OCR processing completed and file downloaded";
    } catch (err: any) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const fetchData = async () => {
      try {
        // Fetch assignment
        const assignmentResponse = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/assignments/${props.assignmentId}`,
          {
            credentials: "include",
          },
        );
        if (!assignmentResponse.ok) {
          throw new Error("Failed to fetch assignment data");
        }
        const assignmentData = await assignmentResponse.json();
        setAssignment(assignmentData.data);

        // Fetch submission details
        const submissionResponse = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/submissions/${props.submissionId}`,
          {
            credentials: "include",
          },
        );

        if (!submissionResponse.ok) {
          throw new Error("Failed to fetch submission data");
        }

        const submissionData = await submissionResponse.json();
        setSubmission(submissionData.data);

        // Fetch associated files
        const filesResponse = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/submission-files?submission-id=${props.submissionId}`,
          {
            credentials: "include",
          },
        );

        if (!filesResponse.ok) {
          throw new Error("Failed to fetch submission files");
        }

        const submissionFilesData = await filesResponse.json();
        setSubmissionFiles(submissionFilesData.data);

        // Fetch judges
        const judgesResponse = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/submissions/${props.submissionId}/judges`,
          {
            credentials: "include",
          },
        );
        if (!judgesResponse.ok) {
          throw new Error("Failed to fetch submission judges");
        }
        const submissionJudgesData = await judgesResponse.json();
        setSubmissionJudges(submissionJudgesData.data || []);
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [props.submissionId]);

  useEffect(() => {
    if (submissionJudges.length === 0) {
      setCalculatedScore(0);
      return;
    }

    let hasPending = false;
    let total = 0;
    let fullMark = 0;

    submissionJudges.forEach((judge) => {
      if (!judge || !judge.status) return;
      const statusId = judge.status.id;
      fullMark += 10;
      if (statusId < 3) {
        hasPending = true;
      } else if (statusId === 3) {
        total += 10;
      }
    });

    setPending(hasPending);
    setCalculatedScore(hasPending ? 0 : total);
    setCalculatedFullMark(fullMark);
  }, [submissionJudges]);

  const getRow = (statId: number) => {
    switch (statId) {
      case 1:
        return (
          // In queue
          <Text
            c={colorScheme === "dark" ? "white" : "dark"}
            size="lg"
            fw={600}
          >
            Pending...
          </Text>
        );
      case 2:
        return (
          // processing
          <Text c="blue.6" size="lg" fw={600}>
            Running...
          </Text>
        );
      case 3:
        return (
          // Accepted
          <Text c="green.6" size="lg" fw={600}>
            Accepted
          </Text>
        );
      case 4:
        return (
          // Wrong Answer
          <Text c="red.6" size="lg" fw={600}>
            Wrong Answer
          </Text>
        );
      case 5:
        return (
          // Time Limit Exceeded
          <Text c="orange.8" size="lg" fw={600}>
            Time Limit Exceeded
          </Text>
        );
      case 6:
        return (
          // Compile Error
          <Text
            c={colorScheme === "dark" ? "gray.4" : "gray.6"}
            size="lg"
            fw={600}
          >
            Compile Error
          </Text>
        );
      // 7-12: Runtime Error
      case 7:
        return (
          // Runtime Error
          <Text c="orange.6" size="lg" fw={600}>
            Runtime Error
          </Text>
        );
      case 8:
        return (
          <Text c="orange.6" size="lg" fw={600}>
            Runtime Error
          </Text>
        );
      case 9:
        return (
          <Text c="orange.6" size="lg" fw={600}>
            Runtime Error
          </Text>
        );
      case 10:
        return (
          <Text c="orange.6" size="lg" fw={600}>
            Runtime Error
          </Text>
        );
      case 11:
        return (
          <Text c="orange.6" size="lg" fw={600}>
            Runtime Error
          </Text>
        );
      case 12:
        return (
          <Text c="orange.6" size="lg" fw={600}>
            Runtime Error
          </Text>
        );
      default:
        return (
          <Text
            c={colorScheme === "dark" ? "gray.4" : "gray.6"}
            size="lg"
            fw={600}
          >
            Unknown Error
          </Text>
        );
    }
  };

  if (loading) {
    return (
      <Group mt="xl" justify="center" align="center" aria-label="Loading group">
        <Loader size="lg" aria-label="Loading spinner" />
        <Text aria-label="Loading text">Loading submission details...</Text>
      </Group>
    );
  }

  if (error) {
    return (
      <Alert title="Error" color="red" mt="xl" aria-label="Error alert">
        {error}
      </Alert>
    );
  }

  return (
    <Group
      bg="var(--mantine-color-body)"
      align="flex-start"
      justify="space-between"
      style={{
        paddingTop: 0,
        paddingLeft: 20,
        paddingRight: 20,
        height: "100%",
        position: "relative",
      }}
      aria-label="Submission view group"
    >
      <LoadingOverlay
        visible={loading}
        overlayProps={{ blur: 2 }}
        aria-label="Loading overlay"
      />
      {error && (
        <Notification
          color="red"
          title="Error"
          onClose={() => setError(null)}
          style={{ position: "absolute", top: 20, right: 20 }}
          aria-label="Error notification"
        >
          {error}
        </Notification>
      )}
      {/* 展示部分，测试结果（和代码） */}
      <Stack
        bg="var(--mantine-color-body)"
        align="flex-start"
        justify="flex-start"
        gap="sm"
        w="80%"
        style={{
          paddingTop: 8,
          paddingLeft: 0,
        }}
        aria-label="Submission result stack"
      >
        {previewFileId ? (
          <FilePreview
            fileIds={[previewFileId]}
            onOcrRequest={handleOcrRequest}
            aria-label="File preview component"
          />
        ) : (
          <Text aria-label="Select file text">Select a file to preview</Text>
        )}

        <Paper
          radius="md"
          w="100%"
          hidden={!submissionJudges}
          aria-label="Judges paper"
        >
          {submissionJudges.map((judge, idx) => {
            if (!judge) return null;
            const isExpand = expandedJudge === judge.token;

            return (
              <Box mx="auto" aria-label={`Judge box: ${judge.token}`}>
                <Card
                  shadow="md"
                  w="100%"
                  radius="md"
                  key={judge.token}
                  mb="md"
                  style={{ cursor: "pointer" }}
                  onClick={() =>
                    setExpandedJudge(
                      expandedJudge === judge.token ? "" : judge.token,
                    )
                  }
                  aria-label={`Judge card: ${judge.token}`}
                >
                  <Group align="center" gap="lg" aria-label="Judge card group">
                    <Text fw={600} aria-label="Judge index">
                      {idx + 1}
                    </Text>
                    <Divider
                      orientation="vertical"
                      aria-label="Judge divider"
                    />
                    {getRow(judge.status.id)}
                    <Box
                      style={{ marginLeft: "auto" }}
                      aria-label="Judge expand icon box"
                    >
                      {expandedJudge === judge.token ? (
                        <ChevronUp aria-label="Collapse icon" />
                      ) : (
                        <ChevronDown aria-label="Expand icon" />
                      )}
                    </Box>
                  </Group>
                </Card>
                {/* 展开后的代码区域 */}
                {expandedJudge === judge.token && (
                  <Collapse
                    in={isExpand}
                    transitionDuration={1000}
                    transitionTimingFunction="linear"
                    aria-label="Judge collapse"
                  >
                    <Card
                      bg="dark"
                      w="100%"
                      radius="md"
                      hidden={props.role === "INSTRUCTOR"}
                      key={judge.token}
                      mb="md"
                      aria-label="Student code result card"
                    >
                      {judge.status.id > 3 ? (
                        <Text
                          size="md"
                          c="red"
                          ff="monospace"
                          aria-label="Code error text"
                        >
                          Check your codes!
                        </Text>
                      ) : judge.status.id < 3 ? (
                        <Text
                          size="md"
                          c="blue"
                          ff="monospace"
                          aria-label="Code pending text"
                        >
                          Please wait...
                        </Text>
                      ) : (
                        <Text
                          size="md"
                          c="green"
                          ff="monospace"
                          aria-label="Code success text"
                        >
                          Congratulations!
                        </Text>
                      )}
                    </Card>
                    <Card
                      bg="dark"
                      w="100%"
                      radius="md"
                      hidden={props.role === "STUDENT"}
                      key={judge.token}
                      mb="md"
                      aria-label="Instructor judge info card"
                    >
                      <Text
                        size="md"
                        c="green"
                        ff="monospace"
                        aria-label="Judge time text"
                      >
                        Time: {judge.time} seconds
                      </Text>
                      <Text
                        size="md"
                        c="green"
                        ff="monospace"
                        aria-label="Judge memory text"
                      >
                        Memory: {judge.memory} KB
                      </Text>
                      {judge.stderr ? (
                        <Text
                          size="md"
                          c="red"
                          ff="monospace"
                          aria-label="Judge stderr text"
                        >
                          Stderr: {judge.stderr}
                        </Text>
                      ) : (
                        <Text
                          size="md"
                          c="green"
                          ff="monospace"
                          aria-label="Judge no stderr text"
                        >
                          No Stderr
                        </Text>
                      )}
                    </Card>
                  </Collapse>
                )}
              </Box>
            );
          })}
        </Paper>

        <Paper
          radius="md"
          w="100%"
          hidden={submissionJudges.length > 0}
          withBorder
          aria-label="Submission success paper"
        >
          <Text aria-label="Submission success text">
            Successfully submitted!!
          </Text>
        </Paper>
      </Stack>

      {/* 选择部分，包括选择文件和分数 */}
      <Stack
        bg="var(--mantine-color-body)"
        align="stretch"
        justify="flex-start"
        gap="sm"
        w="18%"
        style={{
          paddingTop: 8,
          paddingLeft: 0,
          paddingBottom: 20,
          height: "100%",
        }}
        aria-label="Submission sidebar stack"
      >
        <ScoreCard
          realScore={calculatedScore}
          fullMark={calculatedFullMark}
          hidden={props.role === "INSTRUCTOR" || assignment.type === "TEXT"}
          scoreAvailable={true}
          width={"100%"}
          aria-label="Score card"
        />
        <Paper
          w={"100%"}
          radius="md"
          withBorder
          shadow="md"
          mt="md"
          style={{
            flex: 1,
            overflow: "hidden",
          }}
          aria-label="Submission files paper"
        >
          <Box
            style={{
              height: "100%",
              overflowY: "auto",
              paddingTop: 8,
              paddingLeft: 7,
              paddingRight: 7,
            }}
            aria-label="Submission files nav box"
          >
            <NavLink
              key={0}
              href=""
              onClick={() => {
                setActive(0);
                setPreviewFileId(null);
              }}
              active={active === 0}
              label="Result"
              style={{ marginBottom: 8, borderRadius: 8 }}
              aria-label="Result nav link"
            />
            {submissionFiles.map((submissionFile, idx) => (
              <NavLink
                href={`#${submissionFile.file.originalName}`}
                key={submissionFile.file.id}
                label={submissionFile.file.originalName}
                variant="light"
                active={idx + 1 === active}
                onClick={() => {
                  setActive(idx + 1);
                  setPreviewFileId(submissionFile.file.id);
                }}
                style={{ marginBottom: 8, borderRadius: 8 }}
                aria-label={`File nav link: ${submissionFile.file.originalName}`}
              />
            ))}
          </Box>
        </Paper>
      </Stack>

      {/* 文件预览模态框 */}
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
            onOcrRequest={handleOcrRequest}
            aria-label="File preview component"
          />
        )}
      </Modal>
    </Group>
  );
};
