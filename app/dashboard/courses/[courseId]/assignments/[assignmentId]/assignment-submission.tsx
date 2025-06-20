
// AI-generated-content
// tool: Deepseek-R1
// version: latest
// usage: directly use and manual debug
import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useFileDialog } from "@mantine/hooks";
import {
  Button,
  Group,
  List,
  Text,
  Modal,
  Select,
  useMantineColorScheme,
} from "@mantine/core";
import { useAuthRedirect } from "@/app/hooks/useAuthRedirect";
import { FilePreview } from "./file-preview";
import { notifications } from "@mantine/notifications";
import { Languages } from "@/app/interface";

export function AssignmentSubmission(props: {
  assignmentId: number;
  isCode: boolean;
}) {
  const router = useRouter();
  const fileDialog = useFileDialog();
  const [loading, setLoading] = useState<boolean>(false);
  const [previewFileId, setPreviewFileId] = useState<string | null>(null);
  const [languages] = useState<{ id: string; name: string }[]>(Languages);
  const [selectedLanguage, setSelectedLanguage] = useState<string | null>(null);
  const [draftFileIds, setDraftFileIds] = useState<number[]>([]);
  const [draftFiles, setDraftFiles] = useState<{ id: number; filename: string }[]>([]);
  const { colorScheme } = useMantineColorScheme();

  const localStorageKey = `draft-files-assignment-${props.assignmentId}`;

  // Load draft from localStorage
  useEffect(() => {
    const saved = localStorage.getItem(localStorageKey);
    if (saved) {
      const { fileIds, language } = JSON.parse(saved);
      setDraftFileIds(fileIds || []);
      setSelectedLanguage(language || null);
    }
  }, [props.assignmentId]);

  // Fetch draft file metadata from backend
  useEffect(() => {
    if (draftFileIds.length === 0) return;

    const fetchDraftFiles = async () => {
      try {
        const filePromises = draftFileIds.map(async (id) => {
          const response = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/files/${id}?metadata=true`,
            {
              method: "GET",
              credentials: "include",
            }
          );
          if (!response.ok) throw new Error(`Failed to fetch file ${id} metadata`);
          const data = await response.json();
          return { id, filename: data.data.originalName };
        });

        const files = await Promise.all(filePromises);
        setDraftFiles(files);
      } catch (err: any) {
        notifications.show({
          title: "Error",
          message: `Failed to load draft file metadata: ${err.message}`,
          color: "red",
          autoClose: 2000,
        });
      }
    };

    fetchDraftFiles();
  }, [draftFileIds]);

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
        }
      );
      if (!fileResponse.ok) throw new Error("Failed to upload file for preview");
      const fileData = await fileResponse.json();
      setPreviewFileId(fileData.data.id.toString());
      setDraftFileIds((prev) => [...prev, fileData.data.id]);
      setDraftFiles((prev) => [...prev, { id: fileData.data.id, filename: file.name }]);
    } catch (err: any) {
      notifications.show({
        title: "Error",
        message: err.message,
        color: "red",
        autoClose: 2000,
      });
    }
  };

  const handleSaveDraft = async () => {
    if (!fileDialog.files || fileDialog.files.length === 0) {
      notifications.show({
        title: "Error",
        message: "Please select a file first.",
        color: "red",
        autoClose: 2000,
      });
      return;
    }

    try {
      const fileIds: number[] = [];
      const newFiles: { id: number; filename: string }[] = [];
      for (const file of Array.from(fileDialog.files)) {
        const formData = new FormData();
        formData.append("file", file);

        const fileResponse = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/files`,
          {
            method: "POST",
            credentials: "include",
            body: formData,
          }
        );
        if (!fileResponse.ok) throw new Error(`Failed to upload file: ${file.name}`);

        const fileData = await fileResponse.json();
        fileIds.push(fileData.data.id);
        newFiles.push({ id: fileData.data.id, filename: file.name });
      }

      localStorage.setItem(
        localStorageKey,
        JSON.stringify({ fileIds, language: selectedLanguage })
      );
      setDraftFileIds(fileIds);
      setDraftFiles(newFiles);
      notifications.show({
        title: "Draft Saved",
        message: "Draft files saved successfully!",
        color: "green",
        autoClose: 2000,
      });
    } catch (err: any) {
      notifications.show({
        title: "Failed to Save Draft",
        message: err.message,
        color: "red",
        autoClose: 2000,
      });
    }
  };

  const handleSubmit = async () => {
    if ((!fileDialog.files || fileDialog.files.length === 0) && draftFileIds.length === 0) {
      notifications.show({
        title: "Error",
        message: "Please select a file to upload.",
        color: "red",
        autoClose: 2000,
      });
      return;
    }
    if (!selectedLanguage && props.isCode) {
      notifications.show({
        title: "Error",
        message: "Please select a programming language.",
        color: "red",
        autoClose: 2000,
      });
      return;
    }

    setLoading(true);

    try {
      // Step 1: Fetch user ID
      const authResponse = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/auth/me`,
        {
          method: "GET",
          credentials: "include",
        }
      );
      if (!authResponse.ok) throw new Error("Failed to fetch user information");
      const authData = await authResponse.json();
      const userId = authData.data.id;

      // Step 2: Use draft file IDs or upload new files
      let fileIds = draftFileIds;
      if (fileDialog.files && fileDialog.files.length > 0) {
        fileIds = [];
        for (const file of Array.from(fileDialog.files)) {
          const formData = new FormData();
          formData.append("file", file);

          const fileResponse = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/files`,
            {
              method: "POST",
              credentials: "include",
              body: formData,
            }
          );
          if (!fileResponse.ok) throw new Error(`Failed to upload file: ${file.name}`);
          const fileData = await fileResponse.json();
          fileIds.push(fileData.data.id);
        }
      }

      // Step 3: Fetch assignment data
      const assignmentResponse = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/assignments/${props.assignmentId}`,
        {
          method: "GET",
          credentials: "include",
        }
      );
      if (!assignmentResponse.ok) throw new Error("Failed to fetch assignment information");
      const assignmentData = await assignmentResponse.json();
      const courseId = assignmentData.data.course.id;

      // Step 4: Create submission
      const submissionResponse = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/submissions`,
        {
          method: "POST",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            assignment: {
              id: props.assignmentId,
              dueDate: new Date(assignmentData.data.dueDate),
              course: { id: courseId },
            },
            user: { id: userId },
            scoreVisible: false,
            submitTime: new Date(),
            programmingLanguage: selectedLanguage,
          }),
        }
      );
      if (!submissionResponse.ok) throw new Error("Failed to create submission");
      const submissionData = await submissionResponse.json();
      const submissionId = submissionData.data.id;

      // Step 5: Link files to submission
      for (const fileId of fileIds) {
        const submissionFileResponse = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/submission-files`,
          {
            method: "POST",
            credentials: "include",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify({
              submission: { id: submissionId, assignment: { id: props.assignmentId } },
              file: { id: fileId },
            }),
          }
        );
        if (!submissionFileResponse.ok) {
          throw new Error(`Failed to link file ${fileId} to submission ${submissionId}`);
        }
      }

      // Step 6: Create judges
      const judgeResponse = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/submissions/${submissionId}/judges`,
        {
          method: "POST",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
        }
      );
      if (!judgeResponse.ok) throw new Error(`Failed to create judges for submission ${submissionId}`);

      // Step 7: Submission success
      notifications.show({
        title: "Success",
        message: "Files submitted successfully!",
        color: "green",
        autoClose: 2000,
      });
      localStorage.removeItem(localStorageKey);
      fileDialog.reset();
      setDraftFileIds([]);
      setDraftFiles([]);
      router.push(
        `/dashboard/courses/${courseId}/assignments/${props.assignmentId}/submissions/${submissionId}`
      );
    } catch (err: any) {
      notifications.show({
        title: "Error",
        message: err.message,
        color: "red",
        autoClose: 2000,
      });
    } finally {
      setLoading(false);
    }
  };

  const pickedFiles = fileDialog.files
    ? Array.from(fileDialog.files).map((file) => (
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
    ))
    : [];

  const draftFileItems = draftFiles.map((file) => (
    <List.Item key={file.id}>
      {file.filename}
      <Button
        variant="subtle"
        size="xs"
        ml="sm"
        onClick={() => setPreviewFileId(file.id.toString())}
      >
        Preview
      </Button>
    </List.Item>
  ));


  return (
    <div>
      {/* Language selection */}
      <div hidden={!props.isCode}>
        <Select
          label="Programming Language"
          placeholder="Select a programming language"
          data={languages.map((lang) => ({
            value: lang.id,
            label: lang.name,
          }))}
          value={selectedLanguage}
          onChange={setSelectedLanguage}
          searchable
          clearable
          style={{ width: "95%" }}
        />
      </div>
      <Group mt="md">
        <Button
          onClick={fileDialog.open}
          disabled={loading}
          variant={colorScheme === "dark" ? "filled" : "outline"}
        >
          Select File
        </Button>
        {(
          <>
            <Button
              variant="default"
              onClick={() => {
                fileDialog.reset();
                setDraftFileIds([]);
                setDraftFiles([]);
                localStorage.removeItem(localStorageKey);
              }}
              disabled={loading}
              c={colorScheme === "dark" ? "gray" : "dark"}
            >
              Reset
            </Button>
            <Button
              variant={colorScheme === "dark" ? "filled" : "outline"}
              onClick={handleSaveDraft}
              disabled={loading}
            >
              Save Draft
            </Button>
            <Button
              variant={colorScheme === "dark" ? "filled" : "outline"}
              onClick={handleSubmit}
              loading={loading}
            >
              Submit
            </Button>
          </>
        )}
      </Group>
      {pickedFiles.length > 0 && (
        <>
          <Text mt="lg" size="sm">
            Selected Files:
          </Text>
          <List>{pickedFiles}</List>
        </>
      )}
      {draftFileItems.length > 0 && (
        <>
          <Text mt="lg" size="sm" fw={700}>
            Saved Draft Files:
          </Text>
          <List>{draftFileItems}</List>
        </>
      )}
      <Modal
        opened={!!previewFileId}
        onClose={() => setPreviewFileId(null)}
        title="File Preview"
        size="lg"
      >
        {previewFileId && <FilePreview fileIds={[previewFileId]} />}
      </Modal>
    </div>
  );
}
