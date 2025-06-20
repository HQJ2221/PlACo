import { CourseClient } from "./course-client";
export default async function CoursePage({
  params,
}: {
  params: Promise<{ courseId: number }>;
}) {
  const { courseId } = await params;
  return <CourseClient courseId={courseId} />;
}
