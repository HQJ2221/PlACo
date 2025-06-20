// AI-generated-content
// tool: Deepseek-R1
// version: latest
// usage: directly use and manual debug

"use client";

import {
  Button,
  Group,
  Stack,
  TextInput,
  NumberInput,
  Checkbox,
  Loader,
  Alert,
  Text,
  Title,
  FileButton,
  useMantineColorScheme,
} from "@mantine/core";
import { useRouter } from "next/navigation";
import { useState, useEffect } from "react";
import { MoveLeft, FileText, X } from "lucide-react";
import { Testcase } from "@/app/interface";

export const AddTestcasePage = (props: { assignmentId: number }) => {
  const router = useRouter();
  {
    /* 存放所有要添加的testcase */
  }
  const [testcases, setTestcases] = useState<Partial<Testcase>[]>([]);
  {
    /* 当前编辑的testcase */
  }
  const [currentCase, setCurrentCase] =
    useState<Partial<Testcase>>(getDefaultValues());
  {
    /* 当前testcase的输入文件，必须是txt */
  }
  const [stdinFile, setStdinFile] = useState<File | null>(null);
  {
    /* 当前testcase的输出文件，必须是txt */
  }
  const [expectedOutputFile, setExpectedOutputFile] = useState<File | null>(
    null,
  );
  {
    /* 是否显示额外时间限制 */
  }
  const [showExtraTime, setShowExtraTime] = useState(false);
  {
    /* 为文件选择按钮设置一个键值，以支持不同测试用例上传同一份文件（或同文件名文件） */
  }
  const [fileKey, setFileKey] = useState(Date.now());

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { colorScheme } = useMantineColorScheme();

  // 默认值生成函数
  function getDefaultValues(): Partial<Testcase> {
    return {
      cpuTimeLimit: 2.0,
      cpuExtraTime: 0.5,
      wallTimeLimit: 5.0,
      memoryLimit: 128000,
      stackLimit: 64000,
      maxProcessesAndOrThreads: 60,
      maxFileSize: 1024,
      numberOfRuns: 1,
      enableNetwork: false,
      enablePerProcessAndThreadTimeLimit: false,
      enablePerProcessAndThreadMemoryLimit: false,
      redirectStderrToStdout: false,
    };
  }

  // 在 handleAdd 函数前添加文件处理逻辑，实现把TXT文件读取为字符串
  const handleFileUpload = async (file: File) => {
    return new Promise<string>((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (e) => resolve(e.target?.result as string);
      reader.onerror = (e) => reject("File read failed");
      reader.readAsText(file);
    });
  };

  // 修改 handleAdd 函数
  const handleAdd = async () => {
    try {
      let stdinContent = currentCase.stdin || "";
      let outContent = currentCase.expectedOutput || "";

      if (stdinFile) {
        // 验证文件类型
        if (!stdinFile.name.endsWith(".txt")) {
          throw new Error("Only support .txt files");
        }

        // 验证文件大小（限制为 1MB）
        if (stdinFile.size > 1024 * 1024) {
          throw new Error("file size cannot exceed 1MB");
        }

        stdinContent = await handleFileUpload(stdinFile);
      }

      if (expectedOutputFile) {
        // 验证文件类型
        if (!expectedOutputFile.name.endsWith(".txt")) {
          throw new Error("Only support .txt files");
        }

        // 验证文件大小（限制为 1MB）
        if (expectedOutputFile.size > 1024 * 1024) {
          throw new Error("file size cannot exceed 1MB");
        }

        outContent = await handleFileUpload(expectedOutputFile);
      }

      const newCase = {
        ...currentCase,
        assignment: { id: props.assignmentId },
        stdin: stdinContent,
        expectedOutput: outContent,
      };

      setTestcases((prev) => [...prev, newCase]);
      setCurrentCase(getDefaultValues());
      setStdinFile(null); // 清空已选文件
      setExpectedOutputFile(null);
      // 添加成功后重置文件输入
      setFileKey(Date.now());
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleSubmit = async () => {
    try {
      setLoading(true);

      for (const testcase of testcases) {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/test-cases`,
          {
            method: "POST",
            credentials: "include",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(testcase),
          },
        );
        if (!response.ok) throw new Error("Failed to create testcase");
      }
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
      router.back(); // 返回上一页
    }
  };

  return (
    <Stack p="md" maw={800} mx="auto" aria-label="Add testcase page stack">
      <Button
        onClick={() => router.back()}
        variant="subtle"
        color={colorScheme === "dark" ? "white" : "gray"}
        w={120}
        leftSection={<MoveLeft size={18} />}
        aria-label="Back button"
      >
        Back
      </Button>

      <Title order={2} aria-label="Create test cases title">
        Create Test Cases
      </Title>

      {/* 当前输入表单 */}
      <Title order={4} aria-label="Time limits title">
        Time limits
      </Title>
      <Group grow style={{ paddingBottom: 10 }} aria-label="Time limits group">
        <NumberInput
          label="CPU Time Limit (s)"
          value={currentCase.cpuTimeLimit}
          onChange={(v) =>
            setCurrentCase((p) => ({ ...p, cpuTimeLimit: Number(v) }))
          }
          aria-label="CPU time limit input"
        />
        <NumberInput
          label="Wall Time Limit (s)"
          value={currentCase.wallTimeLimit}
          onChange={(v) =>
            setCurrentCase((p) => ({ ...p, wallTimeLimit: Number(v) }))
          }
          aria-label="Wall time limit input"
        />
        <Checkbox
          label={"Need Extra time?"}
          checked={showExtraTime}
          onChange={() => setShowExtraTime(!showExtraTime)}
          style={{ paddingTop: 30 }}
          aria-label="Need extra time checkbox"
        />
      </Group>

      <div hidden={!showExtraTime} aria-label="Extra time input container">
        <NumberInput
          label="CPU Extra Time (s)"
          value={currentCase.cpuExtraTime}
          onChange={(v) =>
            setCurrentCase((p) => ({ ...p, cpuExtraTime: Number(v) }))
          }
          aria-label="CPU extra time input"
        />
      </div>

      <Title order={4} aria-label="Memory limits title">
        Memory limits
      </Title>
      <Group grow aria-label="Memory limits group">
        <NumberInput
          label="Memory Limit (MB)"
          value={currentCase.memoryLimit}
          onChange={(v) =>
            setCurrentCase((p) => ({ ...p, memoryLimit: Number(v) }))
          }
          aria-label="Memory limit input"
        />
        <NumberInput
          label="Stack Limit (MB)"
          value={currentCase.stackLimit}
          onChange={(v) =>
            setCurrentCase((p) => ({ ...p, stackLimit: Number(v) }))
          }
          aria-label="Stack limit input"
        />
      </Group>

      <Title order={4} aria-label="Others title">
        Others
      </Title>
      <Group grow aria-label="Others group">
        <NumberInput
          label="Max Process and/or Thread"
          value={currentCase.maxProcessesAndOrThreads}
          onChange={(v) =>
            setCurrentCase((p) => ({
              ...p,
              maxProcessesAndOrThreads: Number(v),
            }))
          }
          aria-label="Max process/thread input"
        />
        <NumberInput
          label="Max File Size (MB)"
          value={currentCase.maxFileSize}
          onChange={(v) =>
            setCurrentCase((p) => ({ ...p, maxFileSize: Number(v) }))
          }
          aria-label="Max file size input"
        />
        <NumberInput
          label="Number of Runs"
          value={currentCase.numberOfRuns}
          onChange={(v) =>
            setCurrentCase((p) => ({ ...p, numberOfRuns: Number(v) }))
          }
          aria-label="Number of runs input"
        />
      </Group>

      <Group grow aria-label="Options group">
        <Checkbox
          label="Enable Network"
          checked={currentCase.enableNetwork}
          onChange={(e) =>
            setCurrentCase((p) => ({ ...p, enableNetwork: e.target.checked }))
          }
          aria-label="Enable network checkbox"
        />
        <Checkbox
          label="Enable Per Process and Thread Time Limit"
          checked={currentCase.enablePerProcessAndThreadTimeLimit}
          onChange={(e) =>
            setCurrentCase((p) => ({
              ...p,
              enablePerProcessAndThreadTimeLimit: e.target.checked,
            }))
          }
          aria-label="Enable per process/thread time limit checkbox"
        />
        <Checkbox
          label="Enable Per Process and Thread Memory Limit"
          checked={currentCase.enablePerProcessAndThreadMemoryLimit}
          onChange={(e) =>
            setCurrentCase((p) => ({
              ...p,
              enablePerProcessAndThreadMemoryLimit: e.target.checked,
            }))
          }
          aria-label="Enable per process/thread memory limit checkbox"
        />
        <Checkbox
          label="Redirect stderr to stdout"
          checked={currentCase.redirectStderrToStdout}
          onChange={(e) =>
            setCurrentCase((p) => ({
              ...p,
              redirectStderrToStdout: e.target.checked,
            }))
          }
          aria-label="Redirect stderr to stdout checkbox"
        />
      </Group>

      <Title order={4} aria-label="Input and expected output files title">
        Input and Expected Output Files
      </Title>
      <Group grow aria-label="Input/output files group">
        <Stack gap={5}>
          <Group aria-label="Input file group">
            {/* 输入文件读取 */}
            <FileButton
              key={`stdin-${fileKey}`}
              onChange={setStdinFile}
              accept=".txt"
              disabled={!!stdinFile}
              aria-label="Choose input file button"
            >
              {(props) => (
                <Button
                  {...props}
                  variant={colorScheme === "dark" ? "filled" : "outline"}
                  leftSection={<FileText size={16} />}
                  aria-label="Choose input file"
                >
                  Choose Input File
                </Button>
              )}
            </FileButton>
            {stdinFile && (
              <Group gap={4} aria-label="Selected input file group">
                <Text size="sm" aria-label="Input file name">
                  {stdinFile.name}
                </Text>
                <Button
                  variant="transparent"
                  color="red.9"
                  size="compact-xs"
                  onClick={() => setStdinFile(null)}
                  aria-label="Remove input file button"
                >
                  <X size={14} />
                </Button>
              </Group>
            )}
            {/* 输出文件读取 */}
            <FileButton
              key={`out-${fileKey}`}
              onChange={setExpectedOutputFile}
              accept=".txt"
              disabled={!!expectedOutputFile}
              aria-label="Choose output file button"
            >
              {(props) => (
                <Button
                  {...props}
                  variant={colorScheme === "dark" ? "filled" : "outline"}
                  leftSection={<FileText size={16} />}
                  aria-label="Choose output file"
                >
                  Choose Output File
                </Button>
              )}
            </FileButton>
            {expectedOutputFile && (
              <Group gap={4} aria-label="Selected output file group">
                <Text size="sm" aria-label="Output file name">
                  {expectedOutputFile.name}
                </Text>
                <Button
                  variant="transparent"
                  color="red.9"
                  size="compact-xs"
                  onClick={() => setExpectedOutputFile(null)}
                  aria-label="Remove output file button"
                >
                  <X size={14} />
                </Button>
              </Group>
            )}
          </Group>
        </Stack>
      </Group>

      <Group justify="space-between" aria-label="Add testcase group">
        <Button
          onClick={handleAdd}
          variant={colorScheme === "dark" ? "filled" : "outline"}
          color="green"
          aria-label="Add test case button"
        >
          Add Test Case
        </Button>
        <Text aria-label="Testcases prepared text">
          {testcases.length} cases prepared
        </Text>
      </Group>

      {error && (
        <Alert color="red" aria-label="Error alert">
          {error}
        </Alert>
      )}

      <Button
        variant={colorScheme === "dark" ? "filled" : "outline"}
        onClick={handleSubmit}
        loading={loading}
        disabled={testcases.length === 0}
        aria-label="Confirm all button"
      >
        Confirm All ({testcases.length})
      </Button>
    </Stack>
  );
};
