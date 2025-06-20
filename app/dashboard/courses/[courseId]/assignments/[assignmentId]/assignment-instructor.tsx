/*
 * 这是被识别为课程教师后并进入某次作业后，页面主干部分会显示的内容
 * */

// AI-generated-content
// tool: Deepseek-R1
// version: latest
// usage: directly use and manual debug

"use client";
import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  Title,
  Text,
  Table,
  Anchor,
  Loader,
  Alert,
  Stack,
  Group,
  Button,
  Input,
  FileButton,
  Modal,
  List,
  Checkbox,
  Box,
  Divider,
  useMantineColorScheme,
} from "@mantine/core";
import { AssignmentEditModal } from "./assignment-edit-form";
import { AssignmentFiles } from "./assignment-files";
import { IconX, IconUpload } from "@tabler/icons-react";
import { Assignment } from "@/app/interface";
import { useAuthRedirect } from "@/app/hooks/useAuthRedirect";

export const AssignmentInstructor = (props: {
  courseId: number;
  assignmentId: number;
}) => {
  {
    /* 作业数据 */
  }
  const [assignment, setAssignment] = useState<Assignment | null>(null);
  {
    /* 所有提交数据 */
  }
  const [submissions, setSubmissions] = useState<any[]>([]);
  {
    /* 教师用户ID */
  }
  const [userId, setUserId] = useState<number | null>(null);
  {
    /* 教师修改作业相关信息表单（是否打开） */
  }
  const [editModalOpen, setEditModalOpen] = useState(false);
  {
    /* 页面渲染的实时时间 */
  }
  const now = new Date();
  {
    /* 是否超过DDL */
  }
  const [isExpired, setIsExpired] = useState<boolean>(false);
  {
    /* 状态管理 */
  }
  const [savingIds, setSavingIds] = useState<Map<number, [string, boolean]>>(
    new Map(),
  );
  {
    /* 给分超出满分警告 */
  }
  const [scoreWarning, setScoreWarning] = useState("");
  {
    /* 上传新文档的相关状态 */
  }
  const [uploadModalOpen, setUploadModalOpen] = useState(false);
  const [filesToUpload, setFilesToUpload] = useState<File[]>([]);
  const [uploading, setUploading] = useState(false);
  {
    /* 删除文档的相关状态 */
  }
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [allAssignmentFiles, setAllAssignmentFiles] = useState<any[]>([]);
  const [selectedFiles, setSelectedFiles] = useState<number[]>([]);

  {
    /* theme 变化 */
  }
  const { colorScheme } = useMantineColorScheme();

  {
    /* 用户失效返回登录页面 */
  }
  const { handleAuthError } = useAuthRedirect();

  const router = useRouter();

  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        // Fetch user ID
        const userResponse = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/auth/me`,
          {
            credentials: "include",
          },
        );

        if (!userResponse.ok) {
          throw new Error("Failed to fetch user data");
        }

        const userData = await userResponse.json();
        setUserId(userData.data.id);

        // Fetch assignment details
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
        if (!assignmentData.data) {
          throw new Error("Assignment data is null");
        }
        const processedAssignment = {
          ...assignmentData.data,
          publishTime: new Date(assignmentData.data.publishTime),
          dueDate: new Date(assignmentData.data.dueDate),
          createTime: new Date(assignmentData.data.createTime),
        };
        setAssignment(processedAssignment);
        setIsExpired(processedAssignment.dueDate < now);

        // Fetch assignment files
        const filesResponse = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/assignment-files?assignment-id=${props.assignmentId}`,
          {
            credentials: "include",
          },
        );
        if (!filesResponse.ok) {
          throw new Error("Failed to fetch assignment files");
        }
        const filesData = await filesResponse.json();
        setAllAssignmentFiles(filesData.data || []);

        // Fetch submissions based on whether due date is expired
        if (userData.data.id) {
          if (processedAssignment.type === "TEXT") {
            const submissionsResponse = await fetch(
              `${process.env.NEXT_PUBLIC_API_URL}/submissions?assignment-id=${props.assignmentId}&latest=true`,
              {
                credentials: "include",
              },
            );
            if (!submissionsResponse.ok) {
              throw new Error("Failed to fetch submission data");
            }

            const submissionsData = await submissionsResponse.json();
            setSubmissions(submissionsData.data || []);
          } else if (processedAssignment.type === "CODE") {
            const submissionsResponse = await fetch(
              `${process.env.NEXT_PUBLIC_API_URL}/submissions?assignment-id=${props.assignmentId}`,
              {
                credentials: "include",
              },
            );
            if (!submissionsResponse.ok) {
              throw new Error("Failed to fetch submission data");
            }

            const submissionsData = await submissionsResponse.json();
            setSubmissions(submissionsData.data || []);
          }
        }
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const handleSubmit = async (values: Assignment) => {
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/assignments/${props.assignmentId}`,
        {
          method: "PUT",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(values),
        },
      );
      if (!response.ok) {
        throw new Error("Failed to update assignment");
      }
      const data = await response.json();
      setAssignment(data.data);
    } catch (error) {
      console.error("Update failed:", error);
    }
  };

  // 处理文件选择
  const handleFileUpload = async () => {
    setUploading(true);
    try {
      for (const file of Array.from(filesToUpload)) {
        const formData = new FormData();
        formData.append("file", file);
        const fileResponse = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/files`,
          {
            method: "POST",
            credentials: "include",
            body: formData,
          },
        );
        if (!fileResponse.ok) {
          throw new Error(`Failed to upload file: ${file.name}`);
        }
        const fileData = await fileResponse.json();
        const fileId = fileData.data.id;

        const assignmentFileResponse = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/assignment-files`,
          {
            method: "POST",
            credentials: "include",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify({
              assignment: { id: props.assignmentId },
              file: { id: fileId },
            }),
          },
        );
        if (!assignmentFileResponse.ok) {
          throw new Error("Failed to link file to assignment");
        }

        setFilesToUpload([]);
        setUploadModalOpen(false);
      }
    } catch (e: any) {
      alert(`Upload failed, try again.\n${e}`);
    } finally {
      alert("Success!");
      setUploading(false);
    }
  };

  // 处理文件删除
  const handleDeleteFiles = async () => {
    try {
      for (const assignmentFileId of selectedFiles) {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/assignment-files/${assignmentFileId}`,
          {
            method: "DELETE",
            credentials: "include",
          },
        );
        if (!response.ok) {
          throw new Error(`Failed to delete file ${assignmentFileId}`);
        }
      }

      // 更新本地文件列表
      setAllAssignmentFiles((prev) =>
        prev.filter(
          (assignmentFile) => !selectedFiles.includes(assignmentFile.id),
        ),
      );
      setSelectedFiles([]);
      setDeleteModalOpen(false);
      alert("Delete success!");
    } catch (error) {
      alert(error instanceof Error ? error.message : "Unexpected error");
    }
  };

  const handleScore = async (e: any, id: number, fullMark: number) => {
    const formData = new FormData(e.currentTarget);
    const newScore = formData.get(`score-${id}`);
    if (newScore === null) return;

    const scoreValue = parseFloat(newScore as string);
    if (isNaN(scoreValue) || scoreValue > fullMark || scoreValue < 0) {
      alert(`Score must be a number between 0 and ${fullMark}`);
      return;
    }

    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/submissions/${id}`,
        {
          method: "PUT",
          credentials: "include",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ score: scoreValue, scoreVisible: false }),
        },
      );
      if (!response.ok) throw new Error("Failed to save score");

      const updatedData = await response.json();
      setSubmissions((prev) =>
        prev.map((sub) => (sub.id === id ? updatedData.data : sub)),
      );
    } catch (error) {
      alert("Failed to save score. Please try again.");
    }
  };

  const handleSetVisible = async () => {
    try {
      for (const submission of submissions) {
        if (submission.score === null) {
          alert("Please score all submissions before publishing.");
          return;
        }

        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/submissions/${submission.id}`,
          {
            method: "PUT",
            credentials: "include",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify({
              scoreVisible: true,
            }),
          },
        );
        if (!response.ok) {
          throw new Error("Failed to publish scores");
        }
      }
      alert("Success!");
    } catch (error) {
      alert("Try again.");
    }
  };

  const handleDeleteAssignment = async () => {
    if (!confirm("Are you sure you want to delete this assignment?")) return;
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/assignments/${props.assignmentId}`,
        {
          method: "DELETE",
          credentials: "include",
        },
      );
      if (!response.ok) {
        throw new Error("Failed to delete assignment");
      }
      alert("Assignment deleted successfully!");
      router.push(`/dashboard/courses/${props.courseId}`);
    } catch (error) {
      alert("Failed to delete assignment. Please try again.");
    }
  }

  if (loading || assignment === null) {
    return <Loader />;
  }

  if (error) {
    return (
      <Alert title="Error" color="red">
        {error}
      </Alert>
    );
  }

  return (
    <Stack
      bg="var(--mantine-color-body)"
      align="flex-start"
      justify="flex-start"
      gap="sm"
      style={{
        paddingTop: 8,
        paddingLeft: 20,
      }}
    >
      {/* 作业基本信息 */}
      <Group justify="space-between" style={{ width: "95%" }}>
        <Title order={2}>{assignment.title}</Title>
        <Group>
          <Button
            onClick={() => setEditModalOpen(true)}
            variant={colorScheme === "dark" ? "filled" : "outline"}
          >
            Modify
          </Button>
          <Button
            onClick={handleDeleteAssignment}
            color="red"
            variant={colorScheme === "dark" ? "filled" : "outline"}
          >
            Delete
          </Button>
        </Group>
      </Group>
      <Group justify="flex-start" gap="lg" style={{ width: "95%" }}>
        <Text>
          <b>publish time:</b>{" "}
          {new Date(assignment.publishTime + "Z").toLocaleString()}
        </Text>
        <Text>
          <b>due date:</b> {new Date(assignment.dueDate + "Z").toLocaleString()}
        </Text>
      </Group>
      <Box hidden={!assignment.description}>
        <Title order={2} style={{ paddingBottom: 15 }}>
          About
        </Title>
        <Text style={{ paddingLeft: 20 }}>{assignment.description}</Text>
      </Box>

      {/* 修改作业信息，提交表单 */}
      <AssignmentEditModal
        assignment={assignment}
        opened={editModalOpen}
        onClose={() => setEditModalOpen(false)}
        onSubmit={handleSubmit}
      />

      {/* 新增上传模态框 */}
      <Modal
        opened={uploadModalOpen}
        onClose={() => !uploading && setUploadModalOpen(false)}
        title="上传文档"
        size="lg"
      >
        <Group justify="center" mb="md">
          <FileButton
            onChange={(files) =>
              files && setFilesToUpload((prev) => [...prev, ...files])
            }
            multiple
            accept="*/*"
          >
            {(props) => (
              <Button
                {...props}
                leftSection={<IconUpload size={18} />}
                variant={colorScheme === "dark" ? "filled" : "outline"}
              >
                Choose files
              </Button>
            )}
          </FileButton>
        </Group>

        {/* 已选文件列表 */}
        {filesToUpload.length > 0 && (
          <div>
            <Text fw={500} mb="sm">
              已选文件：
            </Text>
            <List spacing="xs" size="sm">
              {filesToUpload.map((file, index) => (
                <List.Item
                  key={index}
                  icon={
                    <Button
                      variant="transparent"
                      color="red"
                      size="compact-xs"
                      onClick={() =>
                        setFilesToUpload((prev) =>
                          prev.filter((_, i) => i !== index),
                        )
                      }
                    >
                      <IconX size={14} />
                    </Button>
                  }
                >
                  <Group gap={4}>
                    <Text>{file.name}</Text>
                    <Text c="dimmed" size="xs">
                      ({(file.size / 1024).toFixed(1)} KB)
                    </Text>
                  </Group>
                </List.Item>
              ))}
            </List>
          </div>
        )}

        {/* 操作按钮 */}
        <Group justify="flex-end" mt="md">
          <Button
            variant="default"
            onClick={() => setUploadModalOpen(false)}
            disabled={uploading}
          >
            Cancel
          </Button>
          <Button
            variant={colorScheme === "dark" ? "filled" : "outline"}
            onClick={handleFileUpload}
            disabled={filesToUpload.length === 0 || uploading}
            loading={uploading}
            leftSection={<IconUpload size={18} />}
          >
            {uploading ? "Uploading..." : "Upload"}
          </Button>
        </Group>
      </Modal>

      {/* 添加删除文档模态框 */}
      <Modal
        opened={deleteModalOpen}
        onClose={() => setDeleteModalOpen(false)}
        title="Manage Docs"
        size="lg"
      >
        {allAssignmentFiles.length === 0 ? (
          <Text c="dimmed">No related docs.</Text>
        ) : (
          <>
            <List spacing="sm">
              {allAssignmentFiles.map((assignmentFile) => (
                <List.Item
                  key={assignmentFile.id}
                  icon={
                    <Checkbox
                      checked={selectedFiles.includes(assignmentFile.id)}
                      onChange={(e) =>
                        setSelectedFiles((prev) =>
                          e.target.checked
                            ? [...prev, assignmentFile.id]
                            : prev.filter((id) => id !== assignmentFile.id),
                        )
                      }
                    />
                  }
                >
                  <Group gap="sm">
                    <Text>{assignmentFile.file.originalName}</Text>
                  </Group>
                </List.Item>
              ))}
            </List>

            <Group justify="space-between" mt="lg">
              <Text c="dimmed" size="sm">
                {selectedFiles.length} files selected
              </Text>
              <Group>
                <Button
                  variant="default"
                  onClick={() => {
                    setDeleteModalOpen(false);
                    setSelectedFiles([]);
                  }}
                >
                  Cancel
                </Button>
                <Button
                  variant={colorScheme === "dark" ? "filled" : "outline"}
                  color="red"
                  onClick={handleDeleteFiles}
                  disabled={selectedFiles.length === 0}
                >
                  Delete
                </Button>
              </Group>
            </Group>
          </>
        )}
      </Modal>

      <div style={{ width: "95%" }}>
        <Divider orientation="horizontal" my="xs" size="xs" />
      </div>
      {/* 作业文件信息 */}
      <Group w={"95%"} justify="space-between">
        <Box w={"80%"}>
          <AssignmentFiles assignmentId={assignment.id} />
        </Box>
        <Stack gap="sm" justify="center">
          <Button
            variant={colorScheme === "dark" ? "filled" : "outline"}
            size={"compact-md"}
            onClick={() => setUploadModalOpen(true)}
          >
            Add documents
          </Button>
          <Button
            variant={colorScheme === "dark" ? "filled" : "outline"}
            size={"compact-md"}
            color="red"
            onClick={() => setDeleteModalOpen(true)}
          >
            Delete documents
          </Button>
        </Stack>
      </Group>
      <div style={{ width: "95%" }}>
        <Divider orientation="horizontal" my="xs" size="xs" />
      </div>

      {/* 如果是代码作业，此处添加测试用例和查看 */}
      <Group hidden={assignment.type !== "CODE"}>
        <Button
          variant={colorScheme === "dark" ? "filled" : "outline"}
          size={"compact-md"}
          color="green"
          onClick={() =>
            router.push(
              `/dashboard/courses/${props.courseId}/assignments/${props.assignmentId}/testcase/new`,
            )
          }
        >
          Add Testcases
        </Button>
        <Button
          size={"compact-md"}
          variant={colorScheme === "dark" ? "filled" : "outline"}
          onClick={() =>
            router.push(
              `/dashboard/courses/${props.courseId}/assignments/${props.assignmentId}/testcase`,
            )
          }
        >
          View Testcases
        </Button>
      </Group>

      <div style={{ width: "95%" }}>
        <Divider orientation="horizontal" my="xs" size="xs" />
      </div>

      {/* 提交信息 */}
      <Table w={"95%"} withTableBorder striped highlightOnHover>
        <Table.Thead>
          <Table.Tr style={{ fontSize: 18 }}>
            <Table.Th w={"8%"}>id</Table.Th>
            <Table.Th w={"15%"}>Student</Table.Th>
            <Table.Th w={"8%"}>SID</Table.Th>
            <Table.Th w={"20%"}>Submit Time</Table.Th>
            <Table.Th>Actions</Table.Th>
            <Table.Th w={"20%"}>Quick score</Table.Th>
            <Table.Th w={"10%"}>State</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody>
          {submissions.map((submission, idx) => (
            <Table.Tr key={submission.id}>
              <Table.Td>{idx + 1}</Table.Td>
              <Table.Td>{submission.user.username || "Unknown user"}</Table.Td>
              <Table.Td>{submission.user.id || "-"}</Table.Td>
              <Table.Td>
                {new Date(submission.submitTime + "Z").toLocaleString()}
                {new Date(submission.submitTime) > assignment.dueDate && (
                  <Text c={"red"}>(late submission)</Text>
                )}
              </Table.Td>
              <Table.Td>
                <Anchor
                  href={`${props.assignmentId}/submissions/${submission.id}`}
                >
                  View Details
                </Anchor>
              </Table.Td>
              <Table.Td>
                <form
                  onSubmit={(e) =>
                    handleScore(e, submission.id, assignment.fullMark)
                  }
                >
                  <Group gap="md" justify="flex-start">
                    <Input
                      w={"30%"}
                      name={`score-${submission.id}`}
                      defaultValue={submission.score?.toString() || ""}
                      placeholder="N/A"
                    />
                    <Button
                      type="submit"
                      variant={colorScheme === "dark" ? "filled" : "outline"}
                      size="compact-sm"
                      disabled={assignment.type === "CODE"}
                    >
                      Save
                    </Button>
                  </Group>
                </form>
              </Table.Td>
              <Table.Td>
                {submission.score
                  ? "Score: " + submission.score.toString()
                  : "Unscore"}
              </Table.Td>
            </Table.Tr>
          ))}
        </Table.Tbody>
      </Table>
      <Group w={"95%"} justify="flex-end">
        <Button
          onClick={handleSetVisible}
          disabled={!isExpired}
          variant={colorScheme === "dark" ? "filled" : "outline"}
        >
          Publish Scores
        </Button>
      </Group>
    </Stack>
  );
};
