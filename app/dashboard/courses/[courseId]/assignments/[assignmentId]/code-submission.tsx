"use client";

import React, { useEffect, useState } from "react";
import { Submission } from "@/app/interface";
import { Table, Anchor, Notification } from "@mantine/core";

export function CodeSubmission(props: {
  userId: number | null;
  assignmentId: number;
  fullMark: number;
  hidden: boolean;
}) {
  const [submissions, setSubmissions] = useState<Submission[]>([]);
  const [error, setError] = useState<any>(null);

  useEffect(() => {
    const fetchSubmissions = async () => {
      try {
        if (props.userId === null) {
          throw new Error("User ID is null");
        }
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/submissions?assignment-id=${props.assignmentId}&user-id=${props.userId}`,
          {
            credentials: "include",
          },
        );

        if (!response.ok) {
          throw new Error("Failed to fetch submissions data");
        }

        const data = await response.json();
        setSubmissions(data.data);
      } catch (err: any) {
        setError(err);
      }
    };

    fetchSubmissions();
  }, []);

  if (error) {
    return (
      <Notification
        color="red"
        onClose={() => setError(null)}
        mb="md"
        aria-label="Error notification"
      >
        {error}
      </Notification>
    );
  }

  if (props.hidden) {
    return null;
  }

  return (
    <Table striped highlightOnHover aria-label="Code submissions table">
      <Table.Thead aria-label="Table header">
        <Table.Tr>
          <Table.Th w={"10%"} aria-label="ID column">
            ID
          </Table.Th>
          <Table.Th aria-label="Submit time column">Submit Time</Table.Th>
          <Table.Th aria-label="Actions column" w={"40%"}>Actions</Table.Th>
        </Table.Tr>
      </Table.Thead>
      <Table.Tbody aria-label="Table body">
        {submissions
          .sort(
            (a, b) =>
              new Date(a.submitTime).getTime() -
              new Date(b.submitTime).getTime(),
          )
          .map((submission, idx) => (
            <Table.Tr
              key={submission.id}
              aria-label={`Submission row: ${submission.id}`}
            >
              <Table.Td aria-label="Index cell">{idx + 1}</Table.Td>
              <Table.Td aria-label="Submit time cell">
                {new Date(submission.submitTime + "Z").toLocaleString()}
              </Table.Td>
              <Table.Td aria-label="Actions cell">
                <Anchor
                  href={`${props.assignmentId}/submissions/${submission.id}`}
                  underline="hover"
                  aria-label="View details link"
                >
                  View Details
                </Anchor>
              </Table.Td>
            </Table.Tr>
          ))}
      </Table.Tbody>
    </Table>
  );
}
