package com.example.restservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "test_cases")
public class TestCase {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;



    private String compilerOptions;
    private String commandLineArguments;
    private Float cpuTimeLimit;
    private Float cpuExtraTime;
    private Float wallTimeLimit;
    private Float memoryLimit;
    private Integer stackLimit;
    private Integer maxProcessesAndOrThreads;
    private Boolean enablePerProcessAndThreadTimeLimit;
    private Boolean enablePerProcessAndThreadMemoryLimit;
    private Integer maxFileSize;
    private Boolean redirectStderrToStdout;
    private Boolean enableNetwork;
    private Integer numberOfRuns;
    private String stdin;
    private String expectedOutput;

    public void updateWithoutId(TestCase testCase) {
        if (testCase.getAssignment() != null) {
            this.assignment = testCase.getAssignment();
        }
        if (testCase.getCompilerOptions() != null) {
            this.compilerOptions = testCase.getCompilerOptions();
        }
        if (testCase.getCommandLineArguments() != null) {
            this.commandLineArguments = testCase.getCommandLineArguments();
        }
        if (testCase.getCpuTimeLimit() != null) {
            this.cpuTimeLimit = testCase.getCpuTimeLimit();
        }
        if (testCase.getCpuExtraTime() != null) {
            this.cpuExtraTime = testCase.getCpuExtraTime();
        }
        if (testCase.getWallTimeLimit() != null) {
            this.wallTimeLimit = testCase.getWallTimeLimit();
        }
        if (testCase.getMemoryLimit() != null) {
            this.memoryLimit = testCase.getMemoryLimit();
        }
        if (testCase.getStackLimit() != null) {
            this.stackLimit = testCase.getStackLimit();
        }
        if (testCase.getMaxProcessesAndOrThreads() != null) {
            this.maxProcessesAndOrThreads = testCase.getMaxProcessesAndOrThreads();
        }
        if (testCase.getEnablePerProcessAndThreadTimeLimit() != null) {
            this.enablePerProcessAndThreadTimeLimit = testCase.getEnablePerProcessAndThreadTimeLimit();
        }
        if (testCase.getEnablePerProcessAndThreadMemoryLimit() != null) {

        }
        if (testCase.getMaxFileSize() != null) {
            this.maxFileSize = testCase.getMaxFileSize();
        }
        if (testCase.getRedirectStderrToStdout() != null) {

        }
        if (testCase.getEnableNetwork() != null) {
            this.enableNetwork = testCase.getEnableNetwork();
        }
        if (testCase.getNumberOfRuns() != null) {
            this.numberOfRuns = testCase.getNumberOfRuns();
        }
        if (testCase.getStdin() != null) {
            this.stdin = testCase.getStdin();
        }
        if (testCase.getExpectedOutput() != null) {
            this.expectedOutput = testCase.getExpectedOutput();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public String getCompilerOptions() {
        return compilerOptions;
    }

    public void setCompilerOptions(String compilerOptions) {
        this.compilerOptions = compilerOptions;
    }

    public String getCommandLineArguments() {
        return commandLineArguments;
    }

    public void setCommandLineArguments(String commandLineArguments) {
        this.commandLineArguments = commandLineArguments;
    }

    public Float getCpuTimeLimit() {
        return cpuTimeLimit;
    }

    public void setCpuTimeLimit(Float cpuTimeLimit) {
        this.cpuTimeLimit = cpuTimeLimit;
    }

    public Float getCpuExtraTime() {
        return cpuExtraTime;
    }

    public void setCpuExtraTime(Float cpuExtraTime) {
        this.cpuExtraTime = cpuExtraTime;
    }

    public Float getWallTimeLimit() {
        return wallTimeLimit;
    }

    public void setWallTimeLimit(Float wallTimeLimit) {
        this.wallTimeLimit = wallTimeLimit;
    }

    public Float getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(Float memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public Integer getStackLimit() {
        return stackLimit;
    }

    public void setStackLimit(Integer stackLimit) {
        this.stackLimit = stackLimit;
    }

    public Integer getMaxProcessesAndOrThreads() {
        return maxProcessesAndOrThreads;
    }

    public void setMaxProcessesAndOrThreads(Integer maxProcessesAndOrThreads) {
        this.maxProcessesAndOrThreads = maxProcessesAndOrThreads;
    }

    public Boolean getEnablePerProcessAndThreadTimeLimit() {
        return enablePerProcessAndThreadTimeLimit;
    }

    public void setEnablePerProcessAndThreadTimeLimit(Boolean enablePerProcessAndThreadTimeLimit) {
        this.enablePerProcessAndThreadTimeLimit = enablePerProcessAndThreadTimeLimit;
    }

    public Boolean getEnablePerProcessAndThreadMemoryLimit() {
        return enablePerProcessAndThreadMemoryLimit;
    }

    public void setEnablePerProcessAndThreadMemoryLimit(Boolean enablePerProcessAndThreadMemoryLimit) {
        this.enablePerProcessAndThreadMemoryLimit = enablePerProcessAndThreadMemoryLimit;
    }

    public Integer getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(Integer maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public Boolean getRedirectStderrToStdout() {
        return redirectStderrToStdout;
    }

    public void setRedirectStderrToStdout(Boolean redirectStderrToStdout) {
        this.redirectStderrToStdout = redirectStderrToStdout;
    }

    public Boolean getEnableNetwork() {
        return enableNetwork;
    }

    public void setEnableNetwork(Boolean enableNetwork) {
        this.enableNetwork = enableNetwork;
    }

    public Integer getNumberOfRuns() {
        return numberOfRuns;
    }

    public void setNumberOfRuns(Integer numberOfRuns) {
        this.numberOfRuns = numberOfRuns;
    }

    public String getStdin() {
        return stdin;
    }

    public void setStdin(String stdin) {
        this.stdin = stdin;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }
}
