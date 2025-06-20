"use client";
import { SubmissionView } from "./submission-view";
import { HEADER_HEIGHT } from "@/app/global";
import { useEffect, useState } from "react";
import { useAuthRedirect } from "@/app/hooks/useAuthRedirect";

export const SubmissionClient = (props: {
  courseId: number;
  assignmentId: number;
  submissionId: number;
}) => {
  const [role, setRole] = useState("UNKNOWN");
  const [error, setError] = useState<any>();
  const { handleAuthError } = useAuthRedirect();

  useEffect(() => {
    const fetchRole = async () => {
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
        const userId = dataMe.data.id;
        const responseRole = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/course-users?user-id=${userId}&course-id=${props.courseId}`,
          {
            credentials: "include",
          },
        );
        if (!responseRole.ok) {
          throw new Error("Failed to fetch course role");
        }
        const dataRole = await responseRole.json();
        setRole(dataRole?.data.role);
      } catch (e: any) {
        setError(e);
      }
    };

    fetchRole();
  }, []);

  if (error) {
    return <div aria-label="Error message">Error: {error.message}</div>;
  }

  return (
    <div
      style={{ height: `calc(96vh - ${HEADER_HEIGHT}px)` }}
      aria-label="Submission client container"
    >
      {role ? (
        <SubmissionView
          courseId={props.courseId}
          assignmentId={props.assignmentId}
          submissionId={props.submissionId}
          role={role}
          aria-label="Submission view component"
        />
      ) : (
        <div aria-label="Error message">Error</div>
      )}
    </div>
  );
};
