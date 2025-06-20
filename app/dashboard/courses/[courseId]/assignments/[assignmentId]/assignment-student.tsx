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
  Loader,
  Stack,
  Group,
  Button,
  Notification,
  List,
  Box,
  Divider,
  Anchor,
} from "@mantine/core";
import { useFileDialog } from "@mantine/hooks";
import { AssignmentFiles } from "./assignment-files";
import { ScoreCard } from "./score-card";
import { CodeSubmission } from "./code-submission";
import { AssignmentSubmission } from "./assignment-submission";
import { useAuthRedirect } from "@/app/hooks/useAuthRedirect";

export const AssignmentStudent = (props: {
  courseId: number;
  assignmentId: number;
}) => {
  const router = useRouter();
  const [assignment, setAssignment] = useState<any>(null);
  const [userId, setUserId] = useState<number | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [latestSubmission, setLatestSubmission] = useState<any>(null);
  const [scoreAvailable, setScoreAvailable] = useState<boolean>(false);
  const { handleAuthError } = useAuthRedirect();

  const [success, setSuccess] = useState<boolean>(false);
  const fileDialog = useFileDialog();
  const pickedFiles = Array.from(fileDialog.files || []).map((file) => (
    <List.Item key={file.name}>{file.name}</List.Item>
  ));

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
        setAssignment(assignmentData.data);

        // Fetch submissions
        if (userData.data.id) {
          const submissionsResponse = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/submissions?assignment-id=${props.assignmentId}&user-id=${userData.data.id}&sort=submit-time&order=desc`,
            {
              credentials: "include",
            },
          );

          if (!submissionsResponse.ok) {
            throw new Error("Failed to fetch submissions");
          }

          const submissionsData = await submissionsResponse.json();
          setLatestSubmission(submissionsData.data);
          if (
            submissionsData.data?.score &&
            submissionsData.data?.scoreVisible > 0
          ) {
            setScoreAvailable(true);
          }
        }
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [props.courseId, props.assignmentId]);

  if (loading) {
    return (
      <Group mt="xl" aria-label="Loading group">
        <Loader size="lg" aria-label="Loading spinner" />
        <Text aria-label="Loading text">Loading assignment details...</Text>
      </Group>
    );
  }

  {
    /* 作业未发布 */
  }
  if (new Date(assignment.publishTime + "Z") > new Date()) {
    return (
      <Stack
        bg="var(--mantine-color-body)"
        align="flex-start"
        justify="flex-start"
        gap="sm"
        style={{
          paddingTop: 0,
          paddingLeft: 20,
        }}
        aria-label="Assignment not available stack"
      >
        <Title order={2} aria-label="Assignment title">
          {assignment.title}
        </Title>
        <Text aria-label="Assignment not available text">
          Assignment is not available yet.
        </Text>
      </Stack>
    );
  }

  return (
    <Stack
      bg="var(--mantine-color-body)"
      align="flex-start"
      justify="flex-start"
      gap="sm"
      style={{
        paddingTop: 0,
        paddingLeft: 20,
      }}
      aria-label="Assignment student stack"
    >
      {/* 作业基本信息 */}
      <Title order={1} aria-label="Assignment title">
        {assignment.title}
      </Title>
      <Group
        justify="flex-start"
        gap="lg"
        style={{ width: "95%" }}
        aria-label="Assignment time group"
      >
        <Text aria-label="Publish time">
          <b>publish time:</b>{" "}
          {new Date(assignment.publishTime + "Z").toLocaleString()}
        </Text>
        <Text aria-label="Due date">
          <b>due date:</b> {new Date(assignment.dueDate + "Z").toLocaleString()}
        </Text>
      </Group>
      <Box
        hidden={!assignment.description}
        aria-label="Assignment description box"
      >
        <Title order={2} style={{ paddingBottom: 15 }} aria-label="About title">
          About
        </Title>
        <Text style={{ paddingLeft: 20 }} aria-label="Assignment description">
          {assignment.description}
        </Text>
      </Box>

      <div style={{ width: "95%" }} aria-label="Divider container">
        <Divider
          orientation="horizontal"
          my="xs"
          size="xs"
          aria-label="Divider"
        />
      </div>
      {/* 显示作业文档 */}
      <Group
        w={"95%"}
        justify="space-between"
        aria-label="Assignment files group"
      >
        <Box w={"80%"} aria-label="Assignment files box">
          <AssignmentFiles
            assignmentId={assignment.id}
            aria-label="Assignment files component"
          />
        </Box>
        <Divider
          orientation="vertical"
          my="sm"
          size="sm"
          hidden={assignment.type === "CODE"}
          aria-label="Vertical divider"
        />
        {/* 显示作业分数（仅非代码作业显示） */}
        <ScoreCard
          realScore={latestSubmission?.score}
          fullMark={assignment.fullMark}
          hidden={assignment.type === "CODE"}
          scoreAvailable={scoreAvailable}
          width={"10%"}
          aria-label="Score card"
        />
      </Group>
      <div style={{ width: "95%" }} aria-label="Divider container">
        <Divider
          orientation="horizontal"
          my="xs"
          size="xs"
          aria-label="Divider"
        />
      </div>

      <Title order={2} mt="lg" aria-label="Submissions title">
        Submissions
      </Title>
      {/* 仅非代码类作业可见 */}
      <Group
        w={"95%"}
        justify="space-between"
        hidden={assignment.type === "CODE"}
        aria-label="Submissions group"
      >
        {/* 提交预览 */}
{/*         <Box w={"80%"} aria-label="Submission preview box">
          You should put the file preview here, or a link for download all
          files(as zip)
        </Box> */}
        <Stack w={"16%"} aria-label="Last submit stack">
          <Title order={4} aria-label="Last submit title">
            Last Submit
          </Title>
          <Text aria-label="Submit time text">
            {"Submit time: "}
            {latestSubmission
              ? new Date(latestSubmission.submitTime + "Z").toLocaleString()
              : "No submission yet"}
          </Text>
          { latestSubmission && (
            <Anchor onClick={() => router.push(`${props.assignmentId}/submissions/${latestSubmission.id}`)}>View Details</Anchor>
          )}
        </Stack>
      </Group>

      {/* 仅代码类作业可见 */}
      <CodeSubmission
        userId={userId}
        assignmentId={assignment.id}
        fullMark={assignment.fullMark}
        hidden={assignment.type === "TEXT"}
        aria-label="Code submission component"
      />

      <AssignmentSubmission
        assignmentId={props.assignmentId}
        isCode={assignment.type === "CODE"}
        aria-label="Assignment submission component"
      />
    </Stack>
  );
};
