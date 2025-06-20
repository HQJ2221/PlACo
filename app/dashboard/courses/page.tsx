// AI-generated-content
// tool: Deepseek-R1
// version: latest
// usage: directly use and manual debug
"use client";
import React, { useEffect, useState, FormEvent } from "react";
import { useRouter } from "next/navigation";
import { Table, Button, Group, TextInput, Loader, Alert } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { User, Course, CourseUser } from "@/app/interface";

export default function CourseList() {
  const router = useRouter();
  const [data, setData] = useState<{ data: Course[] } | null>(null);
  const [instructors, setInstructors] = useState<{ [key: number]: User[] }>({});
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const [editingCourseId, setEditingCourseId] = useState<number | null>(null);
  const [editingCourseName, setEditingCourseName] = useState<string>("");
  const [updating, setUpdating] = useState<boolean>(false);
  const [updateError, setUpdateError] = useState<string | null>(null);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/courses`,
        {
          credentials: "include",
        },
      );

      if (!response.ok) {
        throw new Error("Failed to fetch courses");
      }
      const data = await response.json();
      setData(data);

      // Fetch instructors for each course
      const instructorMap: { [key: number]: User[] } = {};
      for (const course of data.data) {
        const instructorResponse = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/course-users?course-id=${course.id}`,
          {
            credentials: "include",
          },
        );
        if (!instructorResponse.ok) {
          throw new Error(
            `Failed to fetch instructors for course ${course.id}`,
          );
        }
        const instructorData = await instructorResponse.json();
        const courseInstructors = instructorData.data
          .filter((cu: CourseUser) => cu.role === "INSTRUCTOR")
          .map((cu: CourseUser) => cu.user);
        instructorMap[course.id] = courseInstructors;
      }
      setInstructors(instructorMap);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleRowClick = (course: Course) => {
    router.push(`courses/config/${course.id}`);
  };

  const handleUpdateCourse = async (
    e: FormEvent<HTMLFormElement>,
    id: number,
  ) => {
    e.preventDefault();
    if (!editingCourseName.trim()) return;
    setUpdating(true);
    setUpdateError(null);
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/courses/${id}`,
        {
          method: "PUT",
          credentials: "include",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ name: editingCourseName }),
        },
      );
      if (!response.ok) {
        throw new Error("Failed to update course");
      }
      setEditingCourseId(null);
      setEditingCourseName("");
      fetchData();
    } catch (err: any) {
      setUpdateError(err.message);
    } finally {
      setUpdating(false);
    }
  };

  const handleCancelEdit = () => {
    setEditingCourseId(null);
    setEditingCourseName("");
    setUpdateError(null);
  };

  if (loading) return <Loader size={30} aria-label="Loading courses" />;
  if (error)
    return (
      <Alert color="red" aria-label="Error message">
        {error}
      </Alert>
    );

  const rows = data?.data.map((course: Course) => (
    <Table.Tr key={course.id} aria-label={`Course row: ${course.name}`}>
      <Table.Td aria-label="Course name cell">
        {editingCourseId === course.id ? (
          <form
            onSubmit={(e) => handleUpdateCourse(e, course.id)}
            aria-label="Edit course form"
          >
            <Group aria-label="Edit course group">
              <TextInput
                value={editingCourseName}
                onChange={(e) => setEditingCourseName(e.currentTarget.value)}
                placeholder="Enter new course name"
                label="Edit Course"
                required
                aria-label="Edit course name input"
              />
              <Button
                type="submit"
                loading={updating}
                aria-label="Save course button"
              >
                Save
              </Button>
              <Button
                type="button"
                color="gray"
                onClick={handleCancelEdit}
                aria-label="Cancel edit button"
              >
                Cancel
              </Button>
            </Group>
            {updateError && (
              <Alert color="red" aria-label="Update error alert">
                {updateError}
              </Alert>
            )}
          </form>
        ) : (
          <span
            onClick={() => handleRowClick(course)}
            style={{
              cursor: "pointer",
              color: "blue",
              textDecoration: "underline",
            }}
            aria-label="Course name clickable"
          >
            {course.name}
          </span>
        )}
      </Table.Td>
      <Table.Td aria-label="Instructors cell">
        {instructors[course.id]?.length > 0
          ? instructors[course.id]
            .map((instructor) => instructor.username)
            .join(", ")
          : "No instructors"}
      </Table.Td>
    </Table.Tr>
  ));

  return (
    <div aria-label="Courses page container">
      <Button
        onClick={() => router.push("courses/new-course")}
        aria-label="Create course button"
      >
        Create Course
      </Button>
      <Table mt="md" striped withTableBorder aria-label="Courses table">
        <Table.Thead aria-label="Table header">
          <Table.Tr>
            <Table.Th aria-label="Course name column">Course Name</Table.Th>
            <Table.Th aria-label="Instructors column">Instructors</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody aria-label="Table body">{rows}</Table.Tbody>
      </Table>
    </div>
  );
}
