import { AddTestcasePage } from "./new-testcase";

export default async function AssignmentDetail({
  params,
}: {
  params: Promise<{ courseId: number; assignmentId: number }>;
}) {
  const { courseId, assignmentId } = await params;

  return (
    <AddTestcasePage
      assignmentId={assignmentId}
      aria-label="Add testcase page component"
    />
  );
}
