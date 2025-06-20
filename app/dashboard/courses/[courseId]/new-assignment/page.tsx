"use client";
import React, { useState, useEffect } from "react";
import {
  TextInput,
  Textarea,
  Select,
  Button,
  Group,
  Title,
  Text,
  AppShell,
  Loader,
  NumberInput,
  List,
  Checkbox,
  Modal,
  Alert,
  useMantineColorScheme,
} from "@mantine/core";
import { useForm } from "@mantine/form";
import { DateTimePicker } from "@mantine/dates";
import { useFileDialog } from "@mantine/hooks";
import { useRouter } from "next/navigation";
import { use } from "react";
import { useAuthRedirect } from "@/app/hooks/useAuthRedirect";
import { FilePreview } from "../assignments/[assignmentId]/file-preview";
import { notifications } from "@mantine/notifications";

interface AssignmentForm {
  title: string;
  type: string;
  description: string;
  dueDate: string;
  fullMark: number | undefined;
  maxAttempts: number | undefined;
  needOCR: boolean;
}

const CreateAssignment = ({
  params: paramsPromise,
}: {
  params: Promise<{ courseId: string }>;
}) => {
  const params = use(paramsPromise);
  const router = useRouter();
  const fileDialog = useFileDialog();

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [userId, setUserId] = useState<number | null>(null);
  const [previewFileId, setPreviewFileId] = useState<string | null>(null);
  const { colorScheme } = useMantineColorScheme();
  const { handleAuthError } = useAuthRedirect();

  // Fetch current user ID
  useEffect(() => {
    const fetchUser = async () => {
      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/auth/me`,
          {
            credentials: "include",
          },
        );
        if (!response.ok) {
          throw new Error("Failed to fetch user information");
        }
        const data = await response.json();
        setUserId(data.data.id);
      } catch (err: any) {
        setError(err.message || "Failed to fetch user information");
      }
    };
    fetchUser();
  }, []);

  const form = useForm<AssignmentForm>({
    initialValues: {
      title: "",
      type: "",
      description: "",
      dueDate: "",
      fullMark: 100,
      maxAttempts: 3,
      needOCR: false,
    },
    validate: {
      title: (value) =>
        value.length > 0 ? null : "Assignment title is required",
      type: (value) =>
        value.length > 0 ? null : "Assignment type is required",
      description: (value) =>
        value.length > 0 ? null : "Assignment description is required",
      dueDate: (value) => (value ? null : "Due date is required"),
      fullMark: (value) =>
        value && value > 0 ? null : "Full mark must be greater than 0",
      maxAttempts: (value) =>
        value && value > 0 ? null : "Maximum attempts must be greater than 0",
    },
    transformValues: (values) => ({
      ...values,
      fullMark: Number(values.fullMark) || 100,
      maxAttempts: Number(values.maxAttempts) || 3,
    }),
  });

  const handlePreview = async (file: File) => {
    try {
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
      if (!fileResponse.ok)
        throw new Error("Failed to upload file for preview");
      const fileData = await fileResponse.json();
      setPreviewFileId(fileData.data.id);
    } catch (err: any) {
      notifications.show({
        title: "Error",
        message: "Failed to preview file: " + err.message,
        color: "red",
      });
    }
  };

  const pickedFiles = Array.from(fileDialog.files || []).map((file) => (
    <List.Item key={file.name}>
      {file.name}
      <Button
        variant="subtle"
        size="xs"
        ml="sm"
        onClick={() => handlePreview(file)}
      >
        Preview
      </Button>
    </List.Item>
  ));

  const handleSubmit = async (values: AssignmentForm) => {
    if (!userId) {
      notifications.show({
        title: "Error",
        message: "User ID not found",
        color: "red",
      });
      return;
    }

    setLoading(true);

    try {
      // Step 1: Upload files to /files (if any)
      const fileIds: number[] = [];
      if (fileDialog.files && fileDialog.files.length > 0) {
        for (const file of Array.from(fileDialog.files)) {
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
            const errorData = await fileResponse.json();
            throw new Error(
              errorData.message || `Failed to upload file: ${file.name}`,
            );
          }

          const fileData = await fileResponse.json();
          fileIds.push(fileData.data.id);
        }
      }

      // Step 2: Create Assignment
      const dueDateISO = values.dueDate
        ? new Date(values.dueDate).toISOString()
        : new Date().toISOString();
      if (isNaN(new Date(dueDateISO).getTime())) {
        throw new Error("Invalid due date format");
      }

      const assignmentPayload = {
        title: values.title,
        type: values.type,
        description: values.description,
        dueDate: dueDateISO,
        fullMark: values.fullMark ?? 100,
        maxAttempts: values.maxAttempts ?? 3,
        user: { id: userId },
        course: { id: Number(params.courseId) },
        createTime: new Date().toISOString(),
        publishTime: new Date().toISOString(),
        needOCR: values.needOCR,
      };

      const assignmentResponse = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/assignments`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(assignmentPayload),
          credentials: "include",
        },
      );

      if (!assignmentResponse.ok) {
        const errorData = await assignmentResponse.json();
        throw new Error(errorData.message || "Failed to create assignment");
      }

      const assignmentData = await assignmentResponse.json();
      const assignmentId = assignmentData.data.id;

      // Step 3: Link files to assignment via /assignment-files (if any)
      for (const fileId of fileIds) {
        const assignmentFilePayload = {
          assignment: { id: assignmentId },
          file: { id: fileId },
        };

        const assignmentFileResponse = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/assignment-files`,
          {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(assignmentFilePayload),
            credentials: "include",
          },
        );

        if (!assignmentFileResponse.ok) {
          const errorData = await assignmentFileResponse.json();
          throw new Error(
            errorData.message || `Failed to link file ${fileId} to assignment`,
          );
        }
      }

      notifications.show({
        title: "Success",
        message: "Assignment created successfully!",
        color: "green",
      });
      form.reset();
      fileDialog.reset();
      setTimeout(() => {
        router.push(`/dashboard/courses/${params.courseId}`);
      }, 1500);
    } catch (err: any) {
      notifications.show({
        title: "Error",
        message: err.message || "Failed to create assignment",
        color: "red",
      });
    } finally {
      setLoading(false);
    }
  };

  if (error) {
    return (
      <Alert aria-label="Error alert">
        <Text aria-label="Error text">{error}</Text>
        <Button onClick={() => setError(null)} aria-label="Close error button">
          Close
        </Button>
      </Alert>
    );
  }

  if (loading) {
    return (
      <div aria-label="Loading container">
        <Loader size="lg" aria-label="Loading spinner" />
        <Text aria-label="Loading text">Loading...</Text>
      </div>
    );
  }

  return (
    <AppShell.Main
      style={{
        padding: 40,
        minHeight: "calc(100vh - 80px)",
        position: "relative",
      }}
      aria-label="Create assignment main"
    >
      <Title order={1} aria-label="Create assignment title">
        Create New Assignment
      </Title>
      <form
        onSubmit={form.onSubmit(handleSubmit)}
        aria-label="Create assignment form"
      >
        <TextInput
          label="Assignment Title"
          placeholder="Enter assignment title"
          mt="md"
          {...form.getInputProps("title")}
          aria-label="Assignment title input"
        />
        <Select
          label="Assignment Type"
          placeholder="Select type"
          data={[
            { value: "CODE", label: "Code Assignment" },
            { value: "TEXT", label: "Text Assignment" },
          ]}
          mt="md"
          {...form.getInputProps("type")}
          aria-label="Assignment type select"
        />
        <Textarea
          label="Description"
          placeholder="Enter assignment requirements"
          mt="md"
          minRows={4}
          {...form.getInputProps("description")}
          aria-label="Assignment description textarea"
        />
        <DateTimePicker
          label="Due Date"
          placeholder="Select due date"
          mt="md"
          {...form.getInputProps("dueDate")}
          aria-label="Due date picker"
        />
        <NumberInput
          label="Full Mark"
          placeholder="Enter full mark"
          mt="md"
          min={1}
          {...form.getInputProps("fullMark")}
          aria-label="Full mark input"
        />
        <NumberInput
          label="Maximum Attempts"
          placeholder="Enter maximum attempts"
          mt="md"
          min={1}
          {...form.getInputProps("maxAttempts")}
          aria-label="Maximum attempts input"
        />
        <Group mt="md" aria-label="File select group">
          <Button
            onClick={fileDialog.open}
            disabled={loading}
            variant={colorScheme === "dark" ? "filled" : "outline"}
            aria-label="Select file button"
          >
            Select File
          </Button>
          {pickedFiles.length > 0 && (
            <>
              <Button
                variant="default"
                onClick={fileDialog.reset}
                disabled={loading}
                aria-label="Reset files button"
              >
                Reset Files
              </Button>
            </>
          )}
        </Group>
        {pickedFiles.length > 0 && (
          <>
            <Text mt="lg" size="sm" aria-label="Selected files label">
              Selected Files:
            </Text>
            <List aria-label="Selected files list">{pickedFiles}</List>
          </>
        )}
        <Checkbox
          label="Enable OCR for this assignment"
          mt="md"
          {...form.getInputProps("needOCR", { type: "checkbox" })}
          aria-label="Enable OCR checkbox"
        />
        <Group mt="xl" aria-label="Form button group">
          <Button
            type="submit"
            disabled={loading || !userId}
            variant={colorScheme === "dark" ? "filled" : "outline"}
            aria-label="Create assignment button"
          >
            {loading ? (
              <Loader size="sm" aria-label="Loading spinner" />
            ) : (
              "Create Assignment"
            )}
          </Button>
          <Button
            variant="default"
            onClick={() => router.push(`/dashboard/courses/${params.courseId}`)}
            disabled={loading}
            aria-label="Cancel button"
          >
            Cancel
          </Button>
        </Group>
      </form>
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
    </AppShell.Main>
  );
};

export default CreateAssignment;
