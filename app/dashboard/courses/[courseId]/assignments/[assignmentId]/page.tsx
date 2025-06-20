import { AssignmentClient } from "./assignment-client";
import { Text } from "@mantine/core";
export default async function AssignmentDetail({
  params,
}: {
  params: Promise<{ courseId: number; assignmentId: number }>;
}) {
  const { courseId, assignmentId } = await params;

  return (
    <div aria-label="Assignment detail page container">
      <AssignmentClient
        courseId={courseId}
        assignmentId={assignmentId}
        aria-label="Assignment client component"
      />
    </div>
  );
}
