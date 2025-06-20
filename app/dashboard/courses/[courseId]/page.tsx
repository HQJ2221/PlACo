"use client";

import { CourseDetails } from "@/app/dashboard/courses/[courseId]/course-details";
import React, { useState, useEffect } from "react";
import { useAuthRedirect } from "@/app/hooks/useAuthRedirect";

export default function CoursePage({
  params,
}: {
  params: Promise<{ courseId: number }>;
}) {
  const [courseId, setCourseId] = useState<number>(-1);
  const [role, setRole] = useState<string>("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { handleAuthError } = useAuthRedirect();

  useEffect(() => {
    const fetchData = async () => {
      try {
        // 获取课程 ID
        const { courseId } = await params;
        setCourseId(courseId);

        // 获取用户 ID
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

        // 获取用户角色
        const responseRole = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/course-users?user-id=${userId}&course-id=${courseId}`,
          {
            method: "GET",
            credentials: "include",
          },
        );
        if (!responseRole.ok) {
          throw new Error(`Cannot get course user by user ${userId}.`);
        }
        const dataRole = await responseRole.json();
        setRole(dataRole.data.role);

        setLoading(false);
      } catch (err: any) {
        setError(err);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return (
      <div aria-label="Loading course details">Loading course details...</div>
    );
  }

  return (
    <div aria-label="Course page container">
      <CourseDetails
        courseId={courseId}
        roleInCourse={role}
        aria-label="Course details component"
      />
    </div>
  );
}
