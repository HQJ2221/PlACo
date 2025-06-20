// AI-generated-content
// tool: Deepseek-R1
// version: latest
// usage: directly use and manual debug
"use client";
import React, { useEffect, useState } from "react";
import {
  Title,
  Text,
  Table,
  Anchor,
  Loader,
  Alert,
  Paper,
  Group,
} from "@mantine/core";

export const AssignmentDetails = (props: {
  courseId: number;
  assignmentId: number;
}) => {
  const [assignment, setAssignment] = useState<any>(null);
  const [submissions, setSubmissions] = useState<any[]>([]);
  const [userId, setUserId] = useState<number | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

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
        setAssignment(assignmentData);

        // Fetch submissions
        if (userData.data.id) {
          const submissionsResponse = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/submissions?assignment-id=${props.assignmentId}&user-id=${userData.data.id}`,
            {
              credentials: "include",
            },
          );

          if (!submissionsResponse.ok) {
            throw new Error("Failed to fetch submissions");
          }

          const submissionsData = await submissionsResponse.json();
          setSubmissions(submissionsData.data || []);
        }
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [props.courseId, props.assignmentId]);

  // Map submissions to table rows
  const submissionRows = submissions.map((submission) => (
    <Table.Tr
      key={submission.id}
      aria-label={`Submission row: ${submission.title || "Untitled Submission"}`}
    >
      <Table.Td aria-label="Submission title cell">
        {submission.title || "Untitled Submission"}
      </Table.Td>
      <Table.Td aria-label="Submission action cell">
        <Anchor
          href={`${props.assignmentId}/submissions/${submission.id}`}
          aria-label="View details link"
        >
          View Details
        </Anchor>
      </Table.Td>
    </Table.Tr>
  ));

  if (loading) {
    return (
      <Group mt="xl" aria-label="Loading group">
        <Loader size="lg" aria-label="Loading spinner" />
        <Text aria-label="Loading text">Loading assignment details...</Text>
      </Group>
    );
  }

  if (error) {
    return (
      <Alert title="Error" color="red" mt="xl" aria-label="Error alert">
        {error}
      </Alert>
    );
  }

  return (
    <div
      style={{
        paddingLeft: 10,
        paddingTop: 10,
      }}
      aria-label="Assignment details container"
    >
      <Title order={1} mb="lg" aria-label="Assignment details title">
        Assignment Details
      </Title>
      {assignment?.data?.title && (
        <Text size="lg" mb="xs" aria-label="Assignment title">
          <strong>Title:</strong> {assignment.data.title}
        </Text>
      )}
      {assignment?.data?.description && (
        <Text size="lg" mb="md" aria-label="Assignment description">
          <strong>Description:</strong> {assignment.data.description}
        </Text>
      )}
      <Text size="lg" mb="sm" aria-label="Your submissions label">
        Your Submissions
      </Text>
      {submissions.length > 0 ? (
        <Table highlightOnHover aria-label="Submissions table">
          <Table.Thead aria-label="Table header">
            <Table.Tr>
              <Table.Th aria-label="Submission title column">
                Submission Title
              </Table.Th>
              <Table.Th aria-label="Action column">Action</Table.Th>
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody aria-label="Table body">{submissionRows}</Table.Tbody>
        </Table>
      ) : (
        <Text size="sm" mt="md" aria-label="No submissions label">
          No submissions found.
        </Text>
      )}
    </div>
  );
};
