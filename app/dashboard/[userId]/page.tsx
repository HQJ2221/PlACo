import { UserClient } from "./user-client";

export default async function UserPage({
  params,
}: {
  params: Promise<{ userId: number }>;
}) {
  const { userId } = await params;
  return (
    <div aria-label="User Page container">
      <UserClient userId={userId} aria-label="User client component" />
    </div>
  );
}
