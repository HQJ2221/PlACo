import { UserClient } from "./user-client";

export default async function UserPage({
  params,
}: {
  params: Promise<{ userId: number }>;
}) {
  const { userId } = await params;
  return <UserClient userId={userId} />;
}
