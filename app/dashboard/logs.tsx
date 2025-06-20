// AI-generated-content
// tool: Deepseek-R1
// version: latest
// usage: directly use and manual debug
"use client";

import { useState, useEffect } from "react";
import { UserLogs } from "@/app/interface";
import { useAuthRedirect } from "@/app/hooks/useAuthRedirect";
import {
  Table,
  Paper,
  Title,
  Loader,
  Select,
  Group,
  Container,
  ScrollArea,
} from "@mantine/core";
import { DatePicker } from "@mantine/dates";

export function LogsPage() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const { handleAuthError } = useAuthRedirect();

  const [user, setUser] = useState<any>(null);
  const [allUsers, setAllUsers] = useState<any[]>([]);

  const [logs, setLogs] = useState<UserLogs[]>([]);
  const [filteredLogs, setFilteredLogs] = useState<UserLogs[]>([]);
  const [dateRange, setDateRange] = useState<[string | null, string | null]>([
    null,
    null,
  ]);
  const [selectedUser, setSelectedUser] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/auth/me`,
          {
            credentials: "include",
          },
        );

        if (!response.ok) {
          handleAuthError();
          throw new Error("Failed to fetch user data");
        }
        const data = await response.json();
        setUser(data.data);

        if (data.data.role === "ADMIN") {
          const responseAllUsers = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/users`,
            {
              credentials: "include",
            },
          );
          if (!responseAllUsers.ok) {
            throw new Error("Failed to fetch all users data");
          }
          const allUsersData = await responseAllUsers.json();
          setAllUsers(() => {
            return allUsersData.data.map((user: any) => ({
              value: user.id.toString(),
              label: user.username,
            }));
          });
          const responseLogs = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/logs/all`,
            {
              credentials: "include",
            },
          );
          if (!responseLogs.ok) {
            throw new Error("Failed to fetch logs data");
          }
          const logsData = await responseLogs.json();
          setLogs(logsData.data);
          setFilteredLogs(logsData.data);
        } else {
          const responseLogs = await fetch(
            `${process.env.NEXT_PUBLIC_API_URL}/logs/user/${data.data.id}`,
            {
              credentials: "include",
            },
          );
          if (!responseLogs.ok) {
            throw new Error("Failed to fetch logs data");
          }
          const logsData = await responseLogs.json();
          setLogs(logsData.data);
          setFilteredLogs(logsData.data);
        }
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // 过滤日志的逻辑
  useEffect(() => {
    setFilteredLogs(filter());
  }, [user]);

  useEffect(() => {
    setFilteredLogs(filter());
  }, [dateRange]);

  useEffect(() => {
    if (user && user.role === "ADMIN") {
      setFilteredLogs(filter());
    }
  }, [selectedUser]);

  const filter = () => {
    if (!logs || logs.length === 0) return [];
    if (!user) return [];

    return (
      logs
        .filter((log) => {
          const [start, end] = dateRange;
          const logDate = new Date(log.time);
          return (
            (!start || logDate >= new Date(start)) &&
            (!end || logDate <= new Date(end))
          );
        })
        // .filter((log) =>
        //   selectedUser
        //     ? log.user.id.toString() === selectedUser
        //     : true
        // )
        .sort((a, b) => new Date(b.time).getTime() - new Date(a.time).getTime())
    );
  };

  if (loading) {
    return <Loader aria-label="Loading logs" />;
  }

  if (error) {
    return <div aria-label="Error message">Error: {error}</div>;
  }

  return (
    <Container size="xl" aria-label="Logs page container">
      <Paper p="md" shadow="sm" aria-label="Logs paper">
        <Title order={2} mb="md" aria-label="Logs page title">
          User Logs
        </Title>

        <Group mb="md" aria-label="Filter group">
          {/* 时间范围选择 */}
          <DatePicker
            type="range"
            value={dateRange}
            onChange={setDateRange}
            mx="sm"
            aria-label="Date range picker"
          />

          {/* 管理员专属用户搜索 */}
          {user?.role === "ADMIN" && (
            <Select
              placeholder="Search Users"
              data={allUsers}
              value={selectedUser}
              onChange={setSelectedUser}
              clearable
              style={{ width: 200 }}
              aria-label="User select"
            />
          )}
        </Group>

        {/* 日志表格 */}
        <ScrollArea aria-label="Logs table scroll area">
          <Table striped highlightOnHover aria-label="Logs table">
            <Table.Thead aria-label="Table header">
              <Table.Tr>
                <Table.Th aria-label="User column">User</Table.Th>
                <Table.Th aria-label="Time column">Time</Table.Th>
                <Table.Th aria-label="Log column">Log</Table.Th>
              </Table.Tr>
            </Table.Thead>
            <Table.Tbody aria-label="Table body">
              {filteredLogs.map((log) => (
                <Table.Tr
                  key={log.id}
                  aria-label={`Log row: ${log.user.username}`}
                >
                  <Table.Td aria-label="User cell">
                    {log.user.username}
                  </Table.Td>
                  <Table.Td aria-label="Time cell">
                    {new Date(log.time).toLocaleString()}
                  </Table.Td>
                  <Table.Td aria-label="Log cell">{log.logs}</Table.Td>
                </Table.Tr>
              ))}
            </Table.Tbody>
          </Table>
        </ScrollArea>
      </Paper>
    </Container>
  );
}
