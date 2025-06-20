// AI-generated-content
// tool: Deepseek-R1
// version: latest
// usage: directly use and manual debug
"use client";

import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthRedirect } from "@/app/hooks/useAuthRedirect";
import {
  Badge,
  Button,
  Card,
  Grid,
  Group,
  Stack,
  Text,
  Image,
  useMantineColorScheme,
} from "@mantine/core";
import { CourseUser } from "@/app/interface";

export default function DashboardPage() {
  const [courseUsers, setCourseUsers] = useState<CourseUser[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();
  const { colorScheme } = useMantineColorScheme();
  const { handleAuthError } = useAuthRedirect();

  // Fetch Courses Data
  useEffect(() => {
    const fetchData = async () => {
      try {
        const responseMe = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/auth/me`,
          {
            credentials: "include",
          },
        );

        if (!responseMe.ok) {
          throw new Error("Failed to fetch user data");
        }

        const dataMe = await responseMe.json();

        const responseAllCourseUsers = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/course-users?user-id=${dataMe.data.id}`,
          {
            method: "GET",
            credentials: "include",
          },
        );
        if (!responseAllCourseUsers.ok) {
          throw new Error(`Cannot get courses id by user ${dataMe.data.id}.`);
        }
        const dataAllCourseUsers = await responseAllCourseUsers.json();

        setCourseUsers(dataAllCourseUsers?.data || []);
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const handleGoCourse = (courseId: string) => {
    router.push(`/dashboard/courses/${courseId}`);
  };

  if (loading) {
    return <div aria-label="Loading courses">Loading courses...</div>;
  }

  if (error) {
    return <div aria-label="Error message">Error: {error}</div>;
  }

  return (
    <div aria-label="Dashboard Page container">
      {courseUsers.length === 0 ? (
        <Text
          c="dimmed"
          size="lg"
          ta="center"
          mt="md"
          aria-label="No courses message"
        >
          You have not joined any courses
        </Text>
      ) : (
        <Grid columns={5} justify="flex-start" aria-label="Courses grid">
          {courseUsers.map((item: any) => (
            <Grid.Col
              span={1}
              key={item.course.id}
              aria-label={`Course grid column: ${item.course.name}`}
            >
              <Card
                shadow="sm"
                padding="lg"
                radius="md"
                withBorder
                h={250}
                aria-label={`Course card: ${item.course.name}`}
              >
                <Stack h="100%" justify="space-between" aria-label="Card stack">
                  <div aria-label="Course card top section">
                    <Card.Section aria-label="Course image section">
                      <Image
                        src="https://cdn.pixabay.com/photo/2023/01/04/08/27/nature-7696147_1280.jpg"
                        alt="Course cover picture"
                        h={100}
                        aria-label="Course cover image"
                      />
                    </Card.Section>
                    <Group
                      justify="space-between"
                      mt="xs"
                      mb="xs"
                      w={"100%"}
                      aria-label="Course info group"
                    >
                      <Stack w={"70%"} aria-label="Course name stack">
                        <Text fw={600} aria-label="Course name">
                          {item.course.name}
                        </Text>
                      </Stack>
                      <Stack aria-label="Role stack">
                        <Badge
                          color={item.role === "STUDENT" ? "pink" : "grape"}
                          aria-label="User role badge"
                        >
                          {item.role === "STUDENT" ? "Study" : "Teach"}
                        </Badge>
                      </Stack>
                    </Group>
                  </div>
                  <Button
                    variant={colorScheme === "dark" ? "filled" : "outline"}
                    fullWidth
                    radius="md"
                    onClick={() => handleGoCourse(item.course.id)}
                    aria-label={`Enter course button: ${item.course.name}`}
                  >
                    Enter course
                  </Button>
                </Stack>
              </Card>
            </Grid.Col>
          ))}
        </Grid>
      )}
    </div>
  );
}
