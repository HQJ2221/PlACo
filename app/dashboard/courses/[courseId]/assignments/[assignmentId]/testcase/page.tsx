import { TestcaseDetails } from "./testcase";

export default async function AssignmentDetail({
  params,
}: {
  params: Promise<{ courseId: number; assignmentId: number }>;
}) {
  const { courseId, assignmentId } = await params;

  return (
    <div aria-label="Testcase detail page container">
      <TestcaseDetails
        assignmentId={assignmentId}
        aria-label="Testcase details component"
      />
    </div>
  );
}
