import { SubmissionClient } from "./submission-client";
export default async function AssignmentDetail({
  params,
}: {
  params: Promise<{
    courseId: number;
    assignmentId: number;
    submissionId: number;
  }>;
}) {
  const { courseId, assignmentId, submissionId } = await params;
  return (
    <SubmissionClient
      courseId={courseId}
      assignmentId={assignmentId}
      submissionId={submissionId}
      aria-label="Submission client component"
    />
  );
}
