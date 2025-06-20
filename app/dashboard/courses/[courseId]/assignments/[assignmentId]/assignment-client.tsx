// AI-generated-content
// tool: Deepseek-R1
// version: latest
// usage: directly use and manual debug
"use client";
import { AssignmentStudent } from "./assignment-student";
import { AssignmentInstructor } from "./assignment-instructor";
import { useState, useEffect } from "react";
import { useAuthRedirect } from "@/app/hooks/useAuthRedirect";
import { FilePreview } from "./file-preview";

{
  /* 作业详情区域的布局，包含作业详情与提交详情 */
}
export const AssignmentClient = (props: {
  courseId: number;
  assignmentId: number;
}) => {
  const [userId, setUserId] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [role, setRole] = useState<string>("");
  const { handleAuthError } = useAuthRedirect();

  useEffect(() => {
    const fetchUserId = async () => {
      try {
        {
          /* fetch user ID */
        }
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/auth/me`,
          {
            credentials: "include",
          },
        );
        if (!response.ok) {
          throw new Error("Failed to fetch user data");
        }

        const data = await response.json();
        setUserId(data.data.id);

        {
          /* fetch course role */
        }
        const responseRole = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/course-users?user-id=${data.data.id}&course-id=${props.courseId}`,
          {
            method: "GET",
            credentials: "include",
          },
        );
        if (!responseRole.ok) {
          throw new Error(`Cannot get course user by user ${data.data.id}.`);
        }
        const dataRole = await responseRole.json();
        setRole(dataRole?.data.role);
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchUserId();
  }, []);

  if (loading || role === "") {
    return <div aria-label="Loading assignment details">Loading...</div>;
  }
  if (error) {
    return <div aria-label="Error message">Error: {error}</div>;
  }

  if (role === "INSTRUCTOR") {
    return (
      <AssignmentInstructor
        courseId={props.courseId}
        assignmentId={props.assignmentId}
        aria-label="Assignment instructor component"
      />
    );
  } else {
    return (
      <AssignmentStudent
        courseId={props.courseId}
        assignmentId={props.assignmentId}
        aria-label="Assignment student component"
      />
    );
  }
};
