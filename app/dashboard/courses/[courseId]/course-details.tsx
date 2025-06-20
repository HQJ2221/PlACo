// AI-generated-content
// tool: Deepseek-R1
// version: latest
// usage: directly use and manual debug
"use client";
import React, { useEffect, useState } from "react";
import {
  AppShell,
  Text,
  Title,
  Button,
  Paper,
  Stack,
  useMantineColorScheme,
} from "@mantine/core";
import { useRouter } from "next/navigation";

{
  /* 进入课程首次展示的页面，仅供参考 */
}
export const CourseDetails = (props: {
  courseId: number;
  roleInCourse: string;
}) => {
  const [course, setCourse] = useState<any>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const { colorScheme } = useMantineColorScheme();
  const [role, setRole] = useState(
    props.roleInCourse === "STUDENT" ? "student" : "teacher",
  );

  const router = useRouter();

  useEffect(() => {
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
        setCourse(data);
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchCourseData();
  }, [props.courseId]);

  if (loading) {
    return <div aria-label="Loading course details">Loading...</div>;
  }

  if (error) {
    return <div aria-label="Error message">Error: {error}</div>;
  }

  return (
    <AppShell.Main
      style={{
        paddingTop: 20,
        paddingLeft: 40,
        minHeight: "calc(100vh - 80px)",
        position: "relative",
      }}
      aria-label="Course details main area"
    >
      <Title order={1} aria-label="Course name title">
        {course?.data?.name && (
          <p aria-label="Course name">Course Name: {course.data.name}</p>
        )}
      </Title>
      <Stack mt="lg" style={{ paddingLeft: 20 }} aria-label="Course info stack">
        <Paper aria-label="Role info paper">
          <Text size="lg" span={true} aria-label="Role info text">
            You are a {role} in this class.
          </Text>
          <Text aria-label="Role operation info">
            Now you can {role === "student" ? "submit" : "publish"} assignments
            and {role === "student" ? "check your grade" : "grade for students"}
            .
          </Text>
        </Paper>
        <div
          style={{ paddingRight: 20 }}
          aria-label="Create assignment button container"
        >
          {role === "teacher" && (
            <Button
              variant={colorScheme === "dark" ? "filled" : "outline"}
              mt="md"
              onClick={() => router.push(`${props.courseId}/new-assignment`)}
              aria-label="Create assignment button"
            >
              Create Assignment
            </Button>
          )}
        </div>
      </Stack>
    </AppShell.Main>
  );
};
