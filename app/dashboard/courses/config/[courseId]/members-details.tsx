"use client";
import React, { useEffect, useState, useMemo } from "react";
import { Table, Button, Select, Group, Loader, Alert } from "@mantine/core";
import { useDebouncedValue } from "@mantine/hooks";

interface Authority {
  authority: string;
}

interface User {
  id: number;
  email: string;
  username: string;
  password: string;
  role: string;
  authorities: Authority[];
  enabled: boolean;
  credentialsNonExpired: boolean;
  accountNonExpired: boolean;
  accountNonLocked: boolean;
}

interface Course {
  id: number;
  name: string;
}

interface CourseUser {
  id: number;
  course: Course;
  user: User;
  role: string;
}

export const MembersDetails = (props: { courseId: number }) => {
  const [data, setData] = useState<{ data: CourseUser[] } | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [users, setUsers] = useState<User[]>([]);
  const [loadingUsers, setLoadingUsers] = useState<boolean>(false);
  const [newMemberId, setNewMemberId] = useState<string>("");
  const [newMemberRole, setNewMemberRole] = useState<string>("");
  const [searchQuery, setSearchQuery] = useState("");
  const [debouncedQuery] = useDebouncedValue(searchQuery, 500); // Increase debounce time to 500ms

  // Fetch course members data
  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/course-users?course-id=${props.courseId}`,
        {
          credentials: "include",
        },
      );

      if (!response.ok) {
        throw new Error("Failed to get course members");
      }

      const data = await response.json();
      setData(data);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // Search users
  const fetchUsers = async (query: string = "") => {
    setLoadingUsers(true);
    try {
      const url = `${process.env.NEXT_PUBLIC_API_URL}/users${query ? `?search=${encodeURIComponent(query)}` : ""}`;
      const response = await fetch(url, { credentials: "include" });
      if (!response.ok) {
        throw new Error("Failed to get user list");
      }
      const data = await response.json();
      const userList = data.data || data;
      setUsers(
        userList.filter(
          (user: User) =>
            user &&
            typeof user.id === "number" &&
            typeof user.username === "string",
        ),
      );
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoadingUsers(false);
    }
  };

  useEffect(() => {
    fetchData();
    fetchUsers(); // Initially load all users
  }, []);

  useEffect(() => {
    if (debouncedQuery) {
      fetchUsers(debouncedQuery);
    }
  }, [debouncedQuery]);

  // Cache userOptions
  const userOptions = useMemo(
    () =>
      users.map((user) => ({
        value: user.id.toString(),
        label: `${user.username} (ID: ${user.id})`,
      })),
    [users],
  );

  // Add member
  const handleAddMember = async () => {
    if (!newMemberId || !newMemberRole) {
      setError("Please fill in both user and role");
      return;
    }

    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/course-users`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          credentials: "include",
          body: JSON.stringify({
            user: { id: parseInt(newMemberId) },
            course: { id: props.courseId },
            role: newMemberRole,
          }),
        },
      );
      if (!response.ok) throw new Error("Failed to add member");
      await fetchData();
      setNewMemberId("");
      setNewMemberRole("");
      setSearchQuery(""); // Clear search
    } catch (err: any) {
      setError(err.message);
    }
  };

  // Delete member
  const handleDeleteMember = async (memberId: number) => {
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/course-users/${memberId}`,
        {
          method: "DELETE",
          credentials: "include",
        },
      );

      if (!response.ok) {
        throw new Error("Failed to delete member");
      }

      fetchData();
    } catch (err: any) {
      setError(err.message);
    }
  };

  // Table rows
  const rows = data?.data.map((element) => (
    <Table.Tr key={element.id}>
      <Table.Td>{element.user.id}</Table.Td>
      <Table.Td>{element.user.email}</Table.Td>
      <Table.Td>{element.user.username}</Table.Td>
      <Table.Td>{element.role}</Table.Td>
      <Table.Td>
        <Button color="red" onClick={() => handleDeleteMember(element.id)}>
          Delete
        </Button>
      </Table.Td>
    </Table.Tr>
  ));

  if (loading) {
    return <Loader size={30} />;
  }

  if (error) {
    return <Alert color="red">{error}</Alert>;
  }

  return (
    <div>
      <Group pt="md">
        <Select
          placeholder="Search users (username or ID)"
          data={userOptions}
          value={newMemberId}
          onChange={(value) => setNewMemberId(value || "")}
          onSearchChange={setSearchQuery}
          searchable
          nothingFoundMessage="No matching users found"
          disabled={loadingUsers}
          style={{ width: 300 }}
          inputMode="text"
          onFocus={() => console.log("Search input focused")} // Debug focus
          onBlur={() => console.log("Search input blurred")} // Debug focus
        />
        <Select
          placeholder="Select role"
          data={[
            { value: "INSTRUCTOR", label: "Instructor" },
            { value: "STUDENT", label: "Student" },
          ]}
          value={newMemberRole}
          onChange={(value) => setNewMemberRole(value || "")}
          style={{ width: 150 }}
        />
        <Button onClick={handleAddMember}>Add Member</Button>
      </Group>
      <Table striped withTableBorder mt="md">
        <Table.Thead>
          <Table.Tr>
            <Table.Th>ID</Table.Th>
            <Table.Th>Email</Table.Th>
            <Table.Th>Username</Table.Th>
            <Table.Th>Role</Table.Th>
            <Table.Th>Actions</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody>{rows}</Table.Tbody>
      </Table>
    </div>
  );
};
