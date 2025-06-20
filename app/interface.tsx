export interface User {
  id: number;
  username: string;
  email: string;
  role: string;
  userTheme: string;
}

export interface CourseUser {
  user: User;
  role: string;
}

export interface Course {
  id: number;
  name: string;
}

export interface Assignment {
  id: number;
  dueDate: Date;
  fullMark: number;
  publishTime: Date;
  title: string;
  description: string;
  type: string; // 0: "CODE" | 1: "TEXT"
  createTime: Date;
  course: { id: number; name: string };
  needOCR: boolean;
}

export interface AssignmentEditModalProps {
  assignment: Assignment;
  opened: boolean;
  onClose: () => void;
  onSubmit: (values: Assignment) => Promise<void>;
}

export interface ScheduleEntry {
  id: string;
  title: string;
  date: Date;
  description?: string;
}

export interface Submission {
  id: number;
  assignment: Assignment;
  user: User;
  submitTime: Date;
  score: number;
  scoreVisible: boolean;
  programmingLanguage: string;
}

export interface Testcase {
  id: number;
  assignment: { id: number };
  compilerOptions: string;
  commandLineArguments: string;
  cpuTimeLimit: number;
  cpuExtraTime: number;
  wallTimeLimit: number;
  memoryLimit: number;
  stackLimit: number;
  maxProcessesAndOrThreads: number;
  enablePerProcessAndThreadTimeLimit: boolean;
  enablePerProcessAndThreadMemoryLimit: boolean;
  maxFileSize: number;
  redirectStderrToStdout: boolean;
  enableNetwork: boolean;
  numberOfRuns: number;
  stdin: string;
  expectedOutput: string;
}

export interface UserLogs {
  id: number;
  user: {
    id: number;
    username: string;
  };
  logs: string;
  time: Date;
}

export const Languages = [
  { id: "ASSEMBLY_NASM_2_14_02", name: "Assembly (NASM 2.14.02)" },
  { id: "BASH_5_0_0", name: "Bash (5.0.0)" },
  { id: "BASIC_FBC_1_07_1", name: "Basic (FBC 1.07.1)" },
  { id: "C_CLANG_18_1_8", name: "C (Clang 18.1.8)" },
  { id: "C_CLANG_19_1_7", name: "C (Clang 19.1.7)" },
  { id: "C_CLANG_7_0_1", name: "C (Clang 7.0.1)" },
  { id: "CPP_CLANG_7_0_1", name: "C++ (Clang 7.0.1)" },
  { id: "C_GCC_14_1_0", name: "C (GCC 14.1.0)" },
  { id: "CPP_GCC_14_1_0", name: "C++ (GCC 14.1.0)" },
  { id: "C_GCC_7_4_0", name: "C (GCC 7.4.0)" },
  { id: "CPP_GCC_7_4_0", name: "C++ (GCC 7.4.0)" },
  { id: "C_GCC_8_3_0", name: "C (GCC 8.3.0)" },
  { id: "CPP_GCC_8_3_0", name: "C++ (GCC 8.3.0)" },
  { id: "C_GCC_9_2_0", name: "C (GCC 9.2.0)" },
  { id: "CPP_GCC_9_2_0", name: "C++ (GCC 9.2.0)" },
  { id: "CLOJURE_1_10_1", name: "Clojure (1.10.1)" },
  { id: "CSHARP_MONO_6_6_0_161", name: "C# (Mono 6.6.0.161)" },
  { id: "COBOL_GNUCOBOL_2_2", name: "COBOL (GnuCOBOL 2.2)" },
  { id: "COMMON_LISP_SBCL_2_0_0", name: "Common Lisp (SBCL 2.0.0)" },
  { id: "DART_2_19_2", name: "Dart (2.19.2)" },
  { id: "D_DMD_2_089_1", name: "D (DMD 2.089.1)" },
  { id: "ELIXIR_1_9_4", name: "Elixir (1.9.4)" },
  { id: "ERLANG_OTP_22_2", name: "Erlang (OTP 22.2)" },
  { id: "EXECUTABLE", name: "Executable" },
  { id: "FSHARP_DOTNET_CORE_3_1_202", name: "F# (.NET Core SDK 3.1.202)" },
  { id: "FORTRAN_GFORTRAN_9_2_0", name: "Fortran (GFortran 9.2.0)" },
  { id: "GO_1_13_5", name: "Go (1.13.5)" },
  { id: "GO_1_18_5", name: "Go (1.18.5)" },
  { id: "GO_1_22_0", name: "Go (1.22.0)" },
  { id: "GO_1_23_5", name: "Go (1.23.5)" },
  { id: "GROOVY_3_0_3", name: "Groovy (3.0.3)" },
  { id: "HASKELL_GHC_8_8_1", name: "Haskell (GHC 8.8.1)" },
  { id: "JAVAFX_JDK_17_0_6", name: "JavaFX (JDK 17.0.6, OpenJFX 22.0.2)" },
  { id: "JAVA_JDK_17_0_6", name: "Java (JDK 17.0.6)" },
  { id: "JAVA_OPENJDK_13_0_1", name: "Java (OpenJDK 13.0.1)" },
  { id: "JAVASCRIPT_NODE_12_14_0", name: "JavaScript (Node.js 12.14.0)" },
  { id: "JAVASCRIPT_NODE_18_15_0", name: "JavaScript (Node.js 18.15.0)" },
  { id: "JAVASCRIPT_NODE_20_17_0", name: "JavaScript (Node.js 20.17.0)" },
  { id: "JAVASCRIPT_NODE_22_08_0", name: "JavaScript (Node.js 22.08.0)" },
  { id: "KOTLIN_1_3_70", name: "Kotlin (1.3.70)" },
  { id: "KOTLIN_2_1_10", name: "Kotlin (2.1.10)" },
  { id: "LUA_5_3_5", name: "Lua (5.3.5)" },
  { id: "MULTI_FILE_PROGRAM", name: "Multi-file program" },
  { id: "OBJECTIVE_C_CLANG_7_0_1", name: "Objective-C (Clang 7.0.1)" },
  { id: "OCAML_4_09_0", name: "OCaml (4.09.0)" },
  { id: "OCTAVE_5_1_0", name: "Octave (5.1.0)" },
  { id: "PASCAL_FPC_3_0_4", name: "Pascal (FPC 3.0.4)" },
  { id: "PERL_5_28_1", name: "Perl (5.28.1)" },
  { id: "PHP_7_4_1", name: "PHP (7.4.1)" },
  { id: "PHP_8_3_11", name: "PHP (8.3.11)" },
  { id: "PLAIN_TEXT", name: "Plain Text" },
  { id: "PROLOG_GNU_PROLOG_1_4_5", name: "Prolog (GNU Prolog 1.4.5)" },
  { id: "PYTHON_2_7_17", name: "Python (2.7.17)" },
  { id: "PYTHON_3_11_2", name: "Python (3.11.2)" },
  { id: "PYTHON_3_12_5", name: "Python (3.12.5)" },
  { id: "PYTHON_3_13_2", name: "Python (3.13.2)" },
  { id: "PYTHON_3_8_1", name: "Python (3.8.1)" },
  { id: "R_4_0_0", name: "R (4.0.0)" },
  { id: "R_4_4_1", name: "R (4.4.1)" },
  { id: "RUBY_2_7_0", name: "Ruby (2.7.0)" },
  { id: "RUST_1_40_0", name: "Rust (1.40.0)" },
  { id: "RUST_1_85_0", name: "Rust (1.85.0)" },
  { id: "SCALA_2_13_2", name: "Scala (2.13.2)" },
  { id: "SQL_SQLITE_3_27_2", name: "SQL (SQLite 3.27.2)" },
  { id: "SWIFT_5_2_3", name: "Swift (5.2.3)" },
  { id: "TYPESCRIPT_3_7_4", name: "TypeScript (3.7.4)" },
  { id: "TYPESCRIPT_5_0_3", name: "TypeScript (5.0.3)" },
  { id: "TYPESCRIPT_5_6_2", name: "TypeScript (5.6.2)" },
  {
    id: "VISUAL_BASIC_NET_VBNC_0_0_0_5943",
    name: "Visual Basic.Net (vbnc 0.0.0.5943)",
  },
];

// aria-label: Interface definitions for user, course, assignment, etc.
