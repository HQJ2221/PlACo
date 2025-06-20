"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import {
  Button,
  Avatar,
  Input,
  Paper,
  Group,
  Title,
  PasswordInput,
  Stack,
} from "@mantine/core";
import { Github } from "lucide-react";
import { notifications } from "@mantine/notifications";
import { useMantineColorScheme } from "@mantine/core";
import Link from "next/link";

export default function Page() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const [loading, setLoading] = useState(false);
  const { colorScheme, setColorScheme } = useMantineColorScheme();

  useEffect(() => {
    setColorScheme(colorScheme);
  }, []);

  const handleSubmit = async (e: any) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/auth/login`,
        {
          method: "POST",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ username, password }),
        },
      );

      if (!response.ok) {
        throw new Error("Login failed");
      }

      notifications.show({
        title: "Login Successful",
        message: "You have successfully logged in!",
        color: "green",
      });
      router.push("/dashboard");
    } catch (err: any) {
      notifications.show({
        title: "Login Failed",
        message: err.message,
        color: "red",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleLoginViaGithub = async () => {
    window.location.href = `${process.env.NEXT_PUBLIC_API_URL}/oauth2/authorization/github`;
  };

  return (
    <Stack align="center" justify="center" h="100vh">
      <form onSubmit={handleSubmit}>
        <Paper
          radius="md"
          shadow="md"
          w={400}
          p={50}
          bg={colorScheme === "dark" ? "gray.7" : "white"}
        >
          <Title
            c={colorScheme === "dark" ? "white" : "dark"}
            w={"100%"}
            style={{ align: "center", justify: "center", paddingBottom: 20 }}
          >
            Login
          </Title>
          <Input.Wrapper
            c={colorScheme === "dark" ? "white" : "dark"}
            label={"User Name:"}
            size="lg"
          >
            <Input
              placeholder="Input your username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
          </Input.Wrapper>
          <Input.Wrapper
            c={colorScheme === "dark" ? "white" : "dark"}
            label={"Password:"}
            size="lg"
            pt="md"
          >
            <PasswordInput
              placeholder="Input your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </Input.Wrapper>
          <Button
            type="submit"
            variant="filled"
            loading={loading}
            fullWidth
            mt="lg"
          >
            Login
          </Button>
          <Button
            variant="filled"
            color="dark"
            fullWidth
            mt="lg"
            onClick={handleLoginViaGithub}
          >
            <Avatar color="dark" radius="xl">
              <Github color="white" size={16} />
            </Avatar>
            Sign in via Github
          </Button>
        </Paper>
      </form>
    </Stack>
  );
}
