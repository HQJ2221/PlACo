// AI-generated-content
// tool: Deepseek-R1
// version: latest
// usage: directly use and manual debug
"use client";
import React, { useEffect, useState } from "react";
import {
  Stack,
  Group,
  Title,
  AppShell,
  NavLink,
  Box,
  Text,
  useMantineColorScheme,
} from "@mantine/core";
import { FileText, Code, Layers3 } from "lucide-react";
import { useRouter } from "next/navigation";

{
  /* 课程内的侧边栏，展示该课程所有的作业 */
}
export const AssignmentsDetails = (props: { courseId: number }) => {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const { colorScheme } = useMantineColorScheme();

  useEffect(() => {
    const fetchAssignmentsData = async () => {
      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/assignments?course-id=${props.courseId}`,
          {
            credentials: "include",
          },
        );

        if (!response.ok) {
          throw new Error("Failed to fetch assignments data");
        }

        const data = await response.json();
        setData(data);
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchAssignmentsData();
  }, [props.courseId]);

  const isActive = (id: number) => {
    const currentPath = window.location.pathname;
    const currentPathComp = currentPath.split("/");
    const assIndex = currentPathComp.indexOf("assignments");
    if (assIndex === -1) return id === props.courseId;
    return id === Number(currentPathComp[assIndex + 1]);
  };

  const anyActive = () => {
    const currentPath = window.location.pathname;
    const currentPathComp = currentPath.split("/");
    const assIndex = currentPathComp.indexOf("assignments");
    return assIndex !== -1;
  };

  const categorizedAssignments = () => {
    if (!data?.data) return { text: [], code: [] };

    return {
      text: data.data
        .filter((item: any) => item.type === "TEXT")
        .sort(
          (a: any, b: any) =>
            new Date(a.publishTime).getTime() -
            new Date(b.publishTime).getTime(),
        ),
      code: data.data
        .filter((item: any) => item.type === "CODE")
        .sort(
          (a: any, b: any) =>
            new Date(a.publishTime).getTime() -
            new Date(b.publishTime).getTime(),
        ),
    };
  };

  if (loading) {
    return <div aria-label="Loading assignments">Loading...</div>;
  }

  if (error) {
    return <div aria-label="Error message">Error: {error}</div>;
  }

  // 渲染分类区块
  const renderCategory = (
    title: string,
    items: any[],
    icon: React.ReactNode,
  ) => {
    if (items.length === 0)
      return (
        <Stack gap={4} mb="lg" aria-label={`${title} empty stack`}>
          <Group
            gap={8}
            c={colorScheme === "dark" ? "gray.5" : "gray.7"}
            px="sm"
            pb={10}
            aria-label={`${title} group`}
          >
            {icon}
            <Text fw={600} size="sm" aria-label={`${title} label`}>
              {title}
            </Text>
          </Group>
        </Stack>
      );

    return (
      <Stack gap={4} mb="lg" aria-label={`${title} stack`}>
        <Group
          gap={8}
          c={colorScheme === "dark" ? "white" : "dark"}
          px="sm"
          pb={10}
          pl={5}
          aria-label={`${title} group`}
        >
          {icon}
          <Text fw={600} size="sm" aria-label={`${title} label`}>
            {title}
          </Text>
        </Group>
        {items.map((item: any) => (
          <NavLink
            href={`/dashboard/courses/${props.courseId}/assignments/${item.id}`}
            key={item.id}
            active={isActive(item.id)}
            label={item.title}
            color={
              colorScheme === "dark" ? "rgb(0,208,255)" : "rgba(0, 98, 255)"
            }
            variant="light"
            style={{
              label: { fontSize: 16, lineHeight: 1.6 },
              borderRadius: 5,
            }}
            aria-label={`Assignment nav link: ${item.title}`}
          />
        ))}
      </Stack>
    );
  };

  return (
    <AppShell.Navbar p="lg" aria-label="Assignments sidebar navbar">
      <Box w={220} aria-label="Assignments sidebar box">
        <NavLink
          href={`/dashboard/courses/${props.courseId}`}
          key={0}
          active={isActive(props.courseId)}
          label={"Main Page"}
          color={colorScheme === "dark" ? "rgb(0,208,255)" : "rgba(0, 98, 255)"}
          variant="light"
          mb="lg"
          leftSection={<Layers3 size={18} aria-label="Main page icon" />}
          style={{ label: { fontSize: 16, lineHeight: 1.6 }, borderRadius: 5 }}
          aria-label="Main page nav link"
        />
        {renderCategory(
          "Paper Assignments",
          categorizedAssignments().text,
          <FileText size={18} aria-label="Paper assignment icon" />,
        )}
        {renderCategory(
          "Coding Tasks",
          categorizedAssignments().code,
          <Code size={18} aria-label="Coding task icon" />,
        )}
        {categorizedAssignments().text.length === 0 &&
          categorizedAssignments().code.length === 0 && (
            <Text
              c={colorScheme === "dark" ? "gray.5" : "gray.7"}
              size="sm"
              mt="md"
              aria-label="No assignments label"
            >
              No assignments available
            </Text>
          )}
      </Box>
    </AppShell.Navbar>
  );
};
