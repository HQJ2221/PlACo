// AI-generated-content
// tool: Deepseek-R1
// version: latest
// usage: directly use and manual debug

"use client";

import {
  Table,
  Button,
  Loader,
  Alert,
  Group,
  Text,
  Title,
  Collapse,
  Box,
  ScrollArea,
  Code,
  Stack,
  useMantineColorScheme,
} from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { useRouter } from "next/navigation";
import { useState, useEffect } from "react";
import { Testcase } from "@/app/interface";
import { ArrowLeft, Trash, ChevronDown, ChevronUp } from "lucide-react";

export const TestcaseDetails = (props: { assignmentId: number }) => {
  const router = useRouter();
  const [openedIds, setOpenedIds] = useState<Record<number, boolean>>({});
  const [testcases, setTestcases] = useState<Testcase[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { colorScheme } = useMantineColorScheme();

  // 获取测试用例数据
  useEffect(() => {
    const fetchTestcases = async () => {
      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/test-cases?assignment-id=${props.assignmentId}`,
          { credentials: "include" },
        );

        if (!response.ok) throw new Error("Failed to fetch testcases");
        const data = await response.json();
        setTestcases(data.data);
      } catch (err: any) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchTestcases();
  }, [props.assignmentId]);

  // 切换详情展开状态
  const toggleDetails = (id: number) => {
    setOpenedIds((prev) => ({
      ...prev,
      [id]: !prev[id],
    }));
  };

  // 删除测试用例
  const handleDelete = async (testcaseId: number) => {
    try {
      const confirmDelete = confirm("Are you sure to delete this test case?");
      if (!confirmDelete) return;

      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/test-cases/${testcaseId}`,
        {
          method: "DELETE",
          credentials: "include",
        },
      );

      if (!response.ok) throw new Error("Delete failed");

      // 更新本地状态
      setTestcases((prev) => prev.filter((tc) => tc.id !== testcaseId));
    } catch (err: any) {
      setError(err.message);
    }
  };

  if (loading) {
    return (
      <Group justify="center" mt="xl" aria-label="Loading group">
        <Loader aria-label="Loading spinner" />
        <Text aria-label="Loading text">Loading test cases...</Text>
      </Group>
    );
  }

  if (error) {
    return (
      <Alert
        variant="light"
        color="red"
        title="Error"
        mt="xl"
        aria-label="Error alert"
      >
        {error}
      </Alert>
    );
  }

  return (
    <div style={{ padding: 20 }} aria-label="Testcase details container">
      <Group justify="space-between" mb="md" aria-label="Testcase header group">
        <Button
          variant="subtle"
          color={colorScheme === "dark" ? "white" : "gray"}
          onClick={() => router.back()}
          leftSection={<ArrowLeft size={18} />}
          aria-label="Back button"
        >
          Back
        </Button>
        <Title order={2} aria-label="Testcase title">
          Test Cases ({testcases.length})
        </Title>
      </Group>
      <Table striped aria-label="Testcase table">
        <Table.Thead aria-label="Table header">
          <Table.Tr>
            <Table.Th aria-label="CPU time limit column">
              CPU Time Limit
            </Table.Th>
            <Table.Th aria-label="Global time limit column">
              Global Time Limit
            </Table.Th>
            <Table.Th aria-label="Memory limit column">Memory Limit</Table.Th>
            <Table.Th aria-label="Stack limit column">Stack Limit</Table.Th>
            <Table.Th aria-label="Actions column">Actions</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody aria-label="Table body">
          {testcases.map((tc) => (
            <>
              <Table.Tr key={tc.id} aria-label={`Testcase row: ${tc.id}`}>
                <Table.Td aria-label="CPU time limit cell">
                  {tc.cpuTimeLimit} s
                </Table.Td>
                <Table.Td aria-label="Global time limit cell">
                  {tc.wallTimeLimit} s
                </Table.Td>
                <Table.Td aria-label="Memory limit cell">
                  {tc.memoryLimit} MB
                </Table.Td>
                <Table.Td aria-label="Stack limit cell">
                  {tc.stackLimit} MB
                </Table.Td>
                <Table.Td aria-label="Actions cell">
                  <Group gap="xs" aria-label="Actions group">
                    <Button
                      variant={colorScheme === "dark" ? "filled" : "outline"}
                      size="compact-sm"
                      onClick={() => toggleDetails(tc.id!)}
                      leftSection={
                        openedIds[tc.id!] ? (
                          <ChevronUp size={14} />
                        ) : (
                          <ChevronDown size={14} />
                        )
                      }
                      aria-label={
                        openedIds[tc.id!]
                          ? "Hide details button"
                          : "View details button"
                      }
                    >
                      {openedIds[tc.id!] ? "Hide" : "View"}
                    </Button>
                    <Button
                      variant={colorScheme === "dark" ? "filled" : "outline"}
                      color="red"
                      size="compact-sm"
                      onClick={() => handleDelete(tc.id!)}
                      leftSection={<Trash size={14} />}
                      aria-label="Delete testcase button"
                    >
                      Delete
                    </Button>
                  </Group>
                </Table.Td>
              </Table.Tr>
              <Table.Tr
                key={testcases.length + tc.id}
                aria-label={`Testcase details row: ${tc.id}`}
              >
                <Table.Td colSpan={5} p={0}>
                  <Collapse
                    in={openedIds[tc.id!]}
                    aria-label="Testcase collapse"
                  >
                    <Stack
                      align="flex-start"
                      gap={0}
                      pl="xs"
                      pt="xs"
                      pb="md"
                      aria-label="Testcase details stack"
                    >
                      <Text fw={500} mb="xs" aria-label="Input label">
                        Input:
                      </Text>
                      <ScrollArea.Autosize
                        mah={200}
                        w={"100%"}
                        aria-label="Input scroll area"
                      >
                        <Code block aria-label="Input code">
                          {tc.stdin || "No input"}
                        </Code>
                      </ScrollArea.Autosize>
                      <Text fw={500} mb="xs" aria-label="Expected output label">
                        Expected Output:
                      </Text>
                      <ScrollArea.Autosize
                        mah={200}
                        w={"100%"}
                        aria-label="Expected output scroll area"
                      >
                        <Code block aria-label="Expected output code">
                          {tc.expectedOutput || "No output"}
                        </Code>
                      </ScrollArea.Autosize>
                      <Text fw={500} mb="xs" aria-label="Enable label">
                        Enable:
                      </Text>
                      <ScrollArea.Autosize
                        mah={200}
                        w={"100%"}
                        aria-label="Enable scroll area"
                      >
                        <Code block aria-label="Enable code">
                          {tc.enableNetwork ? "Network, " : ""}
                          {tc.enablePerProcessAndThreadTimeLimit
                            ? "Per Process Time Limit, "
                            : ""}
                          {tc.enablePerProcessAndThreadMemoryLimit
                            ? "Per Process Memory Limit, "
                            : ""}
                          {tc.redirectStderrToStdout
                            ? "Redirect Stderr to Stdout, "
                            : ""}
                        </Code>
                      </ScrollArea.Autosize>
                    </Stack>
                  </Collapse>
                </Table.Td>
              </Table.Tr>
            </>
          ))}
        </Table.Tbody>
      </Table>

      {testcases.length === 0 && (
        <Text c="dimmed" mt="md" aria-label="No testcases label">
          No test cases found
        </Text>
      )}
    </div>
  );
};
