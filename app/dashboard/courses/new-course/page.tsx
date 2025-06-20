// AI-generated-content
// tool: Deepseek-R1
// version: latest
// usage: directly use and manual debug
"use client";
import React, { useEffect, useState } from "react";
import {
  Button,
  Group,
  TextInput,
  MultiSelect,
  Select,
  Notification,
  Container,
  Title,
  Tabs,
  Text,
  Table,
  Loader,
} from "@mantine/core";
import { useForm } from "@mantine/form";
import { useRouter } from "next/navigation";
import { useFileDialog } from "@mantine/hooks";

interface User {
  id: number;
  username: string;
  email?: string;
  password?: string;
  role?: string;
}

interface CourseForm {
  courseName: string;
  newInstructorId: string;
  newStudentId: string;
}

interface BatchCourseData {
  name: string;
  instructorIds: string;
  studentIds: string;
}

interface BatchCourseUserData {
  username: string;
  course_name: string;
  role: string;
}

export default function CourseCreation() {
  const router = useRouter();
  const fileDialog = useFileDialog();
  const [users, setUsers] = useState<User[]>([]);
  const [loadingUsers, setLoadingUsers] = useState<boolean>(true);
  const [errorUsers, setErrorUsers] = useState<string | null>(null);
  const [selectedInstructors, setSelectedInstructors] = useState<number[]>([]);
  const [selectedStudents, setSelectedStudents] = useState<number[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<boolean>(false);
  const [parsedCourses, setParsedCourses] = useState<BatchCourseData[]>([]);
  const [parsedCourseUsers, setParsedCourseUsers] = useState<
    BatchCourseUserData[]
  >([]);

  const form = useForm<CourseForm>({
    initialValues: {
      courseName: "",
      newInstructorId: "",
      newStudentId: "",
    },
    validate: {
      courseName: (value) =>
        value.trim() ? null : "Please enter a course name",
      newInstructorId: (value) =>
        value && isNaN(parseInt(value))
          ? "Please enter a valid instructor ID"
          : null,
      newStudentId: (value) =>
        value && isNaN(parseInt(value))
          ? "Please enter a valid student ID"
          : null,
    },
  });

  useEffect(() => {
    const fetchUsers = async () => {
      setLoadingUsers(true);
      setErrorUsers(null);
      try {
        const url = `${process.env.NEXT_PUBLIC_API_URL}/users`;
        const response = await fetch(url, { credentials: "include" });
        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(
            `Failed to get user list: ${response.status} - ${errorText}`,
          );
        }
        const data = await response.json();
        const userList = data.data || data;
        const validUsers = userList.filter(
          (user: User) =>
            user &&
            typeof user.id === "number" &&
            typeof user.username === "string",
        );
        setUsers(validUsers);
      } catch (err: any) {
        setErrorUsers(err.message);
      } finally {
        setLoadingUsers(false);
      }
    };
    fetchUsers();
  }, []);

  const userOptions = users.map((user) => ({
    value: user.id.toString(),
    label: user.username || "Unknown user",
  }));

  const handleAddInstructorById = () => {
    const instructorId = parseInt(form.values.newInstructorId);
    if (!form.validateField("newInstructorId").hasError && instructorId) {
      if (!selectedInstructors.includes(instructorId)) {
        setSelectedInstructors([...selectedInstructors, instructorId]);
      }
      form.setFieldValue("newInstructorId", "");
    }
  };

  const handleAddStudentById = () => {
    const studentId = parseInt(form.values.newStudentId);
    if (!form.validateField("newStudentId").hasError && studentId) {
      if (!selectedStudents.includes(studentId)) {
        setSelectedStudents([...selectedStudents, studentId]);
      }
      form.setFieldValue("newStudentId", "");
    }
  };

  const handleSingleCourseSubmit = async () => {
    if (!form.values.courseName.trim() || selectedInstructors.length === 0) {
      setError(
        "Please fill in all required fields (course name and instructor)",
      );
      form.validate();
      return;
    }
    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      const courseResponse = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/courses`,
        {
          method: "POST",
          credentials: "include",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ name: form.values.courseName }),
        },
      );
      if (!courseResponse.ok) {
        const errorText = await courseResponse.text();
        throw new Error(
          `Failed to create course: ${courseResponse.status} - ${errorText}`,
        );
      }
      const courseData = await courseResponse.json();
      const courseId = courseData.data.id;

      for (const instructorId of selectedInstructors) {
        const instructorResponse = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/course-users`,
          {
            method: "POST",
            credentials: "include",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              course: { id: courseId },
              user: { id: instructorId },
              role: "INSTRUCTOR",
            }),
          },
        );
        if (!instructorResponse.ok) {
          const errorText = await instructorResponse.text();
          throw new Error(
            `Failed to assign instructor: ${instructorResponse.status} - ${errorText}`,
          );
        }
      }

      for (const studentId of selectedStudents) {
        const studentResponse = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/course-users`,
          {
            method: "POST",
            credentials: "include",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              course: { id: courseId },
              user: { id: studentId },
              role: "STUDENT",
            }),
          },
        );
        if (!studentResponse.ok) {
          const errorText = await studentResponse.text();
          throw new Error(
            `Failed to assign student: ${studentResponse.status} - ${errorText}`,
          );
        }
      }

      setSuccess(true);
      form.reset();
      setSelectedInstructors([]);
      setSelectedStudents([]);
      setTimeout(() => router.push("/dashboard/courses"), 1500);
    } catch (err: any) {
      setError(err.message || "An error occurred while creating the course");
    } finally {
      setLoading(false);
    }
  };

  const handleFileSelect = (tab: string) => {
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
        if (lines.length === 0) {
          setError("The file is empty");
          return;
        }

        const headers = lines[0]
          .toLowerCase()
          .split(",")
          .map((h) => h.trim());

        if (tab === "batch") {
          if (
            !headers.includes("name") ||
            !headers.includes("instructorids") ||
            !headers.includes("studentids")
          ) {
            setError(
              "The CSV file must contain name, instructorIds, and studentIds columns",
            );
            return;
          }

          const courses: BatchCourseData[] = [];
          for (let i = 1; i < lines.length; i++) {
            const data = lines[i].split(",").map((d) => d.trim());
            if (data.length < 3) continue;
            courses.push({
              name: data[0],
              instructorIds: data[1],
              studentIds: data[2],
            });
          }

          if (courses.length === 0) {
            setError("There is no valid course data in the file");
            return;
          }

          setParsedCourses(courses);
          setParsedCourseUsers([]);
        } else if (tab === "batch-course-users") {
          if (
            !headers.includes("username") ||
            !headers.includes("course_name") ||
            !headers.includes("role")
          ) {
            setError(
              "The CSV file must contain username, course_name, and role columns",
            );
            return;
          }

          const courseUsers: BatchCourseUserData[] = [];
          for (let i = 1; i < lines.length; i++) {
            const data = lines[i].split(",").map((d) => d.trim());
            if (data.length < 3) continue;
            courseUsers.push({
              username: data[0],
              course_name: data[1],
              role: data[2],
            });
          }

          if (courseUsers.length === 0) {
            setError("There is no valid course-user data in the file");
            return;
          }

          setParsedCourseUsers(courseUsers);
          setParsedCourses([]);
        }

        setError(null);
      };
      reader.onerror = () => setError("Failed to read the file");
      reader.readAsText(file);
    }
  };

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
        `${process.env.NEXT_PUBLIC_API_URL}/courses/batch`,
        {
          method: "POST",
          credentials: "include",
          body: formData,
        },
      );

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(
          errorData.message || "Failed to create courses in batch",
        );
      }

      setSuccess(true);
      fileDialog.reset();
      setParsedCourses([]);
      setTimeout(() => router.push("/dashboard/courses"), 1500);
    } catch (err: any) {
      setError(
        err.message || "An error occurred while creating courses in batch",
      );
    } finally {
      setLoading(false);
    }
  };

  const handleBatchCourseUsersSubmit = async () => {
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
        `${process.env.NEXT_PUBLIC_API_URL}/course-users/batch`,
        {
          method: "POST",
          credentials: "include",
          body: formData,
        },
      );

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(
          errorData.message || "Failed to create course-users in batch",
        );
      }

      setSuccess(true);
      fileDialog.reset();
      setParsedCourseUsers([]);
      setTimeout(() => router.push("/dashboard/courses"), 1500);
    } catch (err: any) {
      setError(
        err.message || "An error occurred while creating course-users in batch",
      );
    } finally {
      setLoading(false);
    }
  };

  if (loadingUsers) {
    return <Loader size={30} />;
  }

  if (errorUsers) {
    return <Notification color="red">{errorUsers}</Notification>;
  }

  return (
    <Container aria-label="Course creation container">
      <Title order={1} aria-label="Create course title">
        Create Course
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
            ? parsedCourses.length > 0
              ? "Courses created successfully!"
              : parsedCourseUsers.length > 0
                ? "Course-users created successfully!"
                : "Course created successfully!"
            : ""}
        </Notification>
      )}
      <Tabs defaultValue="single" mt="md" aria-label="Course creation tabs">
        <Tabs.List aria-label="Tabs list">
          <Tabs.Tab value="single" aria-label="Single course tab">
            Single Course
          </Tabs.Tab>
          <Tabs.Tab value="batch" aria-label="Batch courses tab">
            Batch Courses
          </Tabs.Tab>
          <Tabs.Tab
            value="batch-course-users"
            aria-label="Batch course users tab"
          >
            Batch Course Users
          </Tabs.Tab>
        </Tabs.List>

        <Tabs.Panel value="single" pt="xs" aria-label="Single course panel">
          <form
            onSubmit={form.onSubmit(handleSingleCourseSubmit)}
            aria-label="Single course form"
          >
            <TextInput
              label="Course Name"
              placeholder="Enter the course name"
              required
              mt="md"
              {...form.getInputProps("courseName")}
              aria-label="Course name input"
            />
            <Group mt="md" aria-label="Add instructor group">
              <TextInput
                label="Add Instructor by ID"
                placeholder="Enter instructor ID"
                {...form.getInputProps("newInstructorId")}
                aria-label="Instructor ID input"
              />
              <Button
                onClick={handleAddInstructorById}
                aria-label="Add instructor button"
              >
                Add Instructor
              </Button>
            </Group>
            <MultiSelect
              label="Instructors"
              placeholder="Select instructors"
              data={userOptions}
              value={selectedInstructors.map((id) => id.toString())}
              onChange={(values) =>
                setSelectedInstructors(values.map((v) => parseInt(v)))
              }
              required
              mt="md"
              searchable
              nothingFoundMessage="No matching users found"
              aria-label="Instructors multiselect"
            />
            <Group mt="md" aria-label="Add student group">
              <TextInput
                label="Add Student by ID"
                placeholder="Enter student ID"
                {...form.getInputProps("newStudentId")}
                aria-label="Student ID input"
              />
              <Button
                onClick={handleAddStudentById}
                aria-label="Add student button"
              >
                Add Student
              </Button>
            </Group>
            <MultiSelect
              label="Students"
              placeholder="Select students (optional)"
              data={userOptions}
              value={selectedStudents.map((id) => id.toString())}
              onChange={(values) =>
                setSelectedStudents(values.map((v) => parseInt(v)))
              }
              mt="md"
              searchable
              nothingFoundMessage="No matching users found"
              aria-label="Students multiselect"
            />
            <Group mt="md" aria-label="Form button group">
              <Button
                type="submit"
                loading={loading}
                aria-label="Confirm creation button"
              >
                Confirm Creation
              </Button>
              <Button
                variant="outline"
                onClick={() => router.push("/dashboard/courses")}
                disabled={loading}
                aria-label="Cancel button"
              >
                Cancel
              </Button>
            </Group>
          </form>
        </Tabs.Panel>

        <Tabs.Panel value="batch" pt="xs" aria-label="Batch courses panel">
          <Group mt="md" aria-label="Batch file group">
            <Button
              onClick={() => fileDialog.open()}
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
              Selected file: {fileDialog.files[0].name}
            </Text>
          )}
          {parsedCourses.length > 0 && (
            <Table mt="md" aria-label="Parsed courses table">
              <thead>
                <tr>
                  <th>Course Name</th>
                  <th>Instructor IDs</th>
                  <th>Student IDs</th>
                </tr>
              </thead>
              <tbody>
                {parsedCourses.map((course, index) => (
                  <tr key={index} aria-label={`Parsed course row ${index}`}>
                    <td>{course.name}</td>
                    <td>{course.instructorIds}</td>
                    <td>{course.studentIds}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}
        </Tabs.Panel>

        <Tabs.Panel
          value="batch-course-users"
          pt="xs"
          aria-label="Batch course users panel"
        >
          <Group mt="md" aria-label="Batch course users file group">
            <Button
              onClick={() => fileDialog.open()}
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
                  onClick={handleBatchCourseUsersSubmit}
                  loading={loading}
                  aria-label="Submit batch course users button"
                >
                  Submit
                </Button>
              </>
            )}
          </Group>
          {fileDialog.files && fileDialog.files.length > 0 && (
            <Text mt="lg" size="sm" aria-label="Selected file label">
              Selected file: {fileDialog.files[0].name}
            </Text>
          )}
          {parsedCourseUsers.length > 0 && (
            <Table mt="md" aria-label="Parsed course users table">
              <thead>
                <tr>
                  <th>Username</th>
                  <th>Course Name</th>
                  <th>Role</th>
                </tr>
              </thead>
              <tbody>
                {parsedCourseUsers.map((courseUser, index) => (
                  <tr
                    key={index}
                    aria-label={`Parsed course user row ${index}`}
                  >
                    <td>{courseUser.username}</td>
                    <td>{courseUser.course_name}</td>
                    <td>{courseUser.role}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}
        </Tabs.Panel>
      </Tabs>
    </Container>
  );
}
