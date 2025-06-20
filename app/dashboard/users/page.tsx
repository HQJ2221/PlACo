"use client";
import React, { useEffect, useState } from "react";
import { Group, Table, Button, Container, Anchor } from "@mantine/core";
import { useRouter } from "next/navigation";
import { User } from "@/app/interface";

interface Authority {
  authority: string;
}

export default function CoursesPage() {
  const router = useRouter();
  const [data, setData] = useState<{ data: User[] } | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/users`, {
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error("Failed to fetch courses");
      }

      const data = await response.json();
      setData(data);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const rows = data?.data.map((element) => (
    <Table.Tr key={element.id}>
      <Table.Td>{element.id}</Table.Td>
      <Table.Td>{element.email}</Table.Td>
      <Table.Td>
        <Anchor onClick={() => router.push(`users/${element.id}`)}>
          {element.username}
        </Anchor>
      </Table.Td>
      <Table.Td>{element.role}</Table.Td>
    </Table.Tr>
  ));

  if (loading) {
    return <div aria-label="Loading container">Loading...</div>;
  }

  if (error) {
    return <div aria-label="Error message">{error}</div>;
  }

  return (
    <Container aria-label="Users page container">
      <Group mb="md" aria-label="New user group">
        <Button
          variant="filled"
          onClick={() => router.push("users/new")}
          aria-label="New user button"
        >
          New
        </Button>
      </Group>
      <Table striped withTableBorder aria-label="Users table">
        <Table.Thead aria-label="Table header">
          <Table.Tr>
            <Table.Th aria-label="ID column">ID</Table.Th>
            <Table.Th aria-label="Email column">Email</Table.Th>
            <Table.Th aria-label="Username column">Username</Table.Th>
            <Table.Th aria-label="Role column">Role</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody aria-label="Table body">{rows}</Table.Tbody>
      </Table>
    </Container>
  );
}
