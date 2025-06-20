"use client";

import React, { useEffect, useState } from "react";
import { AssignmentsDetails } from "./assignments-details";
import { AppShell } from "@mantine/core";

export default function Layout({
  params,
  children,
}: {
  params: Promise<{ courseId: number }>;
  children: React.ReactNode;
}) {
  const [courseId, setCourseId] = useState<number>(-1);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const { courseId } = await params;
        setCourseId(courseId);
        setLoading(false);
      } catch (error) {
        console.error("Error fetching course ID:", error);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return <div aria-label="Loading assignments">Loading assignments...</div>;
  }

  return (
    <AppShell
      navbar={{
        width: 270,
        breakpoint: "sm",
        collapsed: { mobile: false },
      }}
      aria-label="Course layout shell"
    >
      {/* 作业列表侧边栏 */}
      <AssignmentsDetails
        courseId={courseId}
        aria-label="Assignments sidebar"
      />

      {/* 课程主要内容区域 */}
      <div aria-label="Course main content">{children}</div>
    </AppShell>
  );
}
