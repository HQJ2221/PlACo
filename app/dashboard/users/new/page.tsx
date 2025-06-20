"use client";
import React, { useState } from "react";
import {
  Button,
  Group,
  TextInput,
  Notification,
  Container,
  Title,
  Tabs,
  Text,
  Table,
  Select,
} from "@mantine/core";
import { useForm } from "@mantine/form";
import { useRouter } from "next/navigation";
import { useFileDialog } from "@mantine/hooks";
import { useAuthRedirect } from "@/app/hooks/useAuthRedirect";

interface UserForm {
  email: string;
  username: string;
  password: string;
  role: string;
}

interface BatchUserData {
  username: string;
  email: string;
  password: string;
  role: string;
}

export default function CreateUser() {
  const router = useRouter();
  const fileDialog = useFileDialog();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<boolean>(false);
  const [parsedUsers, setParsedUsers] = useState<BatchUserData[]>([]);
  const { handleAuthError } = useAuthRedirect();

  // Form for single user creation
  const form = useForm<UserForm>({
    initialValues: {
      email: "",
      username: "",
      password: "",
      role: "",
    },
    validate: {
      email: (value) =>
        /^\S+@\S+$/.test(value) ? null : "Please enter a valid email",
      username: (value) => (value.length > 0 ? null : "Username is required"),
      password: (value) =>
        value.length >= 6 ? null : "Password must be at least 6 characters",
      role: (value) => (value ? null : "Please select a role"),
    },
  });

  // Handle single user creation
  const handleSingleUserSubmit = async (values: UserForm) => {
    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/users`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(values),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Failed to create user");
      }

      setSuccess(true);
      form.reset();
      setTimeout(() => router.push("/dashboard/users"), 1500);
    } catch (err: any) {
      setError(err.message || "Error occurred while creating user");
      // handleAuthError(err);
    } finally {
      setLoading(false);
    }
  };

  // Handle file selection and parse CSV
  const handleFileSelect = () => {
    if (fileDialog.files && fileDialog.files.length > 0) {
      const file = fileDialog.files[0];
      if (!file.name.endsWith(".csv")) {
        setError("Only CSV files are supported");
        return;
      }

      const reader = new FileReader();
      reader.onload = (e) => {
        const text = e.target?.result as string;
        if (!text) {
          setError("The file is empty");
          return;
        }

        const lines = text.split("\n").filter((line) => line.trim() !== "");
        const users: BatchUserData[] = [];
        const headers = lines[0]
          .toLowerCase()
          .split(",")
          .map((h) => h.trim());

        // Validate headers
        if (
          !headers.includes("username") ||
          !headers.includes("email") ||
          !headers.includes("password") ||
          !headers.includes("role")
        ) {
          setError(
            "CSV file must contain username, email, password, role columns",
          );
          return;
        }

        // Parse rows
        for (let i = 1; i < lines.length; i++) {
          const data = lines[i].split(",").map((d) => d.trim());
          if (data.length < 4) continue; // Skip invalid rows
          users.push({
            username: data[0],
            email: data[1],
            password: data[2],
            role: data[3],
          });
        }

        if (users.length === 0) {
          setError("There is no valid user data in the file");
          return;
        }

        setParsedUsers(users);
        setError(null);
      };
      reader.onerror = () => setError("Failed to read file");
      reader.readAsText(file);
    }
  };

  // Handle batch user creation
  const handleBatchSubmit = async () => {
    if (!fileDialog.files || fileDialog.files.length === 0) {
      setError("Please select a CSV file first");
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      const formData = new FormData();
      formData.append("file", fileDialog.files[0]);

      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/users/batch`,
        {
          method: "POST",
          credentials: "include",
          body: formData,
        },
      );

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Failed to create users in batch");
      }

      setSuccess(true);
      fileDialog.reset();
      setParsedUsers([]);
      setTimeout(() => router.push("/dashboard/users"), 1500);
    } catch (err: any) {
      setError(err.message || "Error occurred while creating users in batch");
      // handleAuthError(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container aria-label="Create user container">
      <Title order={1} aria-label="Create user title">
        Create User
      </Title>
      {error && (
        <Notification
          color="red"
          onClose={() => setError(null)}
          mt="md"
          aria-label="Error notification"
        >
          {error}
        </Notification>
      )}
      {success && (
        <Notification
          color="green"
          onClose={() => setSuccess(false)}
          mt="md"
          aria-label="Success notification"
        >
          {success
            ? "User created successfully!"
            : "Users created successfully in batch!"}
        </Notification>
      )}
      <Tabs defaultValue="single" mt="md" aria-label="Create user tabs">
        <Tabs.List aria-label="Tabs list">
          <Tabs.Tab value="single" aria-label="Single user tab">
            Single User
          </Tabs.Tab>
          <Tabs.Tab value="batch" aria-label="Batch users tab">
            Batch Users
          </Tabs.Tab>
        </Tabs.List>

        <Tabs.Panel value="single" pt="xs" aria-label="Single user panel">
          <form
            onSubmit={form.onSubmit(handleSingleUserSubmit)}
            aria-label="Single user form"
          >
            <TextInput
              withAsterisk
              label="Email"
              placeholder="Enter email"
              mt="md"
              {...form.getInputProps("email")}
              aria-label="Email input"
            />
            <TextInput
              withAsterisk
              label="Username"
              placeholder="Enter username"
              mt="md"
              {...form.getInputProps("username")}
              aria-label="Username input"
            />
            <TextInput
              withAsterisk
              label="Password"
              placeholder="Enter password"
              type="password"
              mt="md"
              {...form.getInputProps("password")}
              aria-label="Password input"
            />
            <Select
              withAsterisk
              label="Role"
              placeholder="Select role"
              data={[
                { value: "USER", label: "User" },
                { value: "ADMIN", label: "Admin" },
              ]}
              mt="md"
              {...form.getInputProps("role")}
              aria-label="Role select"
            />
            <Group mt="md" aria-label="Form button group">
              <Button
                type="submit"
                disabled={loading}
                aria-label="Create user button"
              >
                Create User
              </Button>
              <Button
                variant="outline"
                onClick={() => router.push("/dashboard/users")}
                disabled={loading}
                aria-label="Cancel button"
              >
                Cancel
              </Button>
            </Group>
          </form>
        </Tabs.Panel>

        <Tabs.Panel value="batch" pt="xs" aria-label="Batch users panel">
          <Group mt="md" aria-label="Batch file group">
            <Button
              onClick={fileDialog.open}
              disabled={loading}
              aria-label="Select CSV file button"
            >
              Select CSV File
            </Button>
            {fileDialog.files && fileDialog.files.length > 0 && (
              <>
                <Button
                  variant="default"
                  onClick={fileDialog.reset}
                  disabled={loading}
                  aria-label="Reset file button"
                >
                  Reset
                </Button>
                <Button
                  onClick={handleBatchSubmit}
                  loading={loading}
                  aria-label="Submit batch button"
                >
                  Submit
                </Button>
              </>
            )}
          </Group>
          {fileDialog.files && fileDialog.files.length > 0 && (
            <Text mt="lg" size="sm" aria-label="Selected file label">
              Selected File: {fileDialog.files[0].name}
            </Text>
          )}
        </Tabs.Panel>
      </Tabs>
    </Container>
  );
}
