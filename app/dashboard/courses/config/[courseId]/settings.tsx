"use client";
import React, { useEffect, useState } from "react";
import {
  Title,
  TextInput,
  Button,
  Group,
  Alert,
  Loader,
  Stack,
} from "@mantine/core";
import { useRouter } from "next/navigation";
interface Course {
  id: number;
  name: string;
}

export const Settings = (props: { courseId: number }) => {
  const router = useRouter();
  const [course, setCourse] = useState<Course | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [saving, setSaving] = useState<boolean>(false);
  const [deleting, setDeleting] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [name, setName] = useState<string>("");

  const fetchCourseData = async () => {
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/courses/${props.courseId}`,
        {
          credentials: "include",
        },
      );

      if (!response.ok) {
        throw new Error("Failed to fetch course data");
      }
      const data = await response.json();
      setCourse(data?.data);
      setName(data?.data?.name);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCourseData();
  }, [props.courseId]);

  const handleRename = async () => {
    setSaving(true);
    setError(null);
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/courses/${props.courseId}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
          credentials: "include",
          body: JSON.stringify({ name }),
        },
      );

      if (!response.ok) {
        throw new Error("Failed to update course name");
      }

      const data = await response.json();
      setCourse(data.data);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!confirm("Are you sure you want to delete this course?")) {
      return;
    }
    setDeleting(true);
    setError(null);
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/courses/${props.courseId}`,
        {
          method: "DELETE",
          credentials: "include",
        },
      );

      if (!response.ok) {
        throw new Error("Failed to delete course");
      }

      router.push("/admin/courses");
      alert("Course deleted successfully");
    } catch (err: any) {
      setError(err.message);
    } finally {
      setDeleting(false);
    }
  };

  if (loading) {
    return <Loader size={30} />;
  }

  if (error) {
    return <Alert color="red">{error}</Alert>;
  }

  return (
    <Stack gap="md" pt="md">
      <Title order={1}>General</Title>
      <TextInput
        label="Course Name"
        value={name}
        onChange={(e) => setName(e.target.value)}
      />
      <Button onClick={handleRename} loading={saving} color="blue" w={200}>
        Rename Course
      </Button>
      <Title order={1}>Danger Zone</Title>
      <Button onClick={handleDelete} loading={deleting} color="red" w={200}>
        Delete Course
      </Button>
    </Stack>
  );
};
