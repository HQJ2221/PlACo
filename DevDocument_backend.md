<span style="font-family: 微软雅黑, sans-serif;">

# PlACo后端开发文档

## 1. 项目概述

本项目是一个基于Spring Boot的RESTful API服务，旨在提供课程管理、作业提交、用户认证等功能。主要功能包括：

用户认证与授权（支持OAuth2）。
课程管理（创建、查询、更新课程信息）。
作业管理（提交、评分、文件上传）。
文件处理（上传、转换、存储）。

本文档将详细介绍项目的架构、API端点、数据模型及实现细节。

## 2. 架构设计

### 技术栈

框架：Spring Boot
安全：Spring Security + OAuth2
数据访问：Spring Data JPA + Hibernate
数据库：PostgreSQL
构建工具：Gradle

### 模块划分

auth：认证和授权模块。
controller：API控制器模块。
model：数据模型模块。
repository：数据访问模块。
service：业务逻辑模块。

项目代码主要位于src/main/java/com/example/restservice目录下，配置文件位于src/main/resources/application.properties。

## 3. 认证与授权

认证使用Spring Security结合OAuth2实现，支持基于角色的访问控制。主要类包括：

### 3.1 SecurityConfiguration

作用：配置Spring Security和OAuth2的安全策略。

关键实现：

CSRF：禁用CSRF保护以支持RESTful API。
请求授权：

允许OPTIONS方法、根路径（/）、/auth/login和/oauth2/authorization/*无需认证。

其他请求需认证。

OAuth2登录：使用自定义Oauth2LoginSuccessHandler处理登录成功逻辑。

密码加密：使用BCryptPasswordEncoder进行密码加密。

CORS：支持跨域请求，允许GET、POST、PUT、DELETE和OPTIONS方法。

认证管理：配置AuthenticationManager以使用DaoAuthenticationProvider，结合UserDetailsService和PasswordEncoder。

代码位置：com.example.restservice.auth.SecurityConfiguration

### 3.2 AuthController

作用：处理用户登录、登出和获取当前用户信息。

API端点：

**POST /auth/login：**

功能：处理用户名/密码登录。

请求：LoginRequest（包含username和password）。

响应：ApiResponse（成功返回"Login Successful"，失败抛出异常）。

实现：使用AuthenticationManager验证凭据，保存认证信息到SecurityContext，记录用户IP和登录日志。
    
**GET /auth/me：**

功能：获取当前用户信息。

响应：ApiResponse（成功返回User对象，失败返回"Unauthenticated"）。

实现：从SecurityContext获取认证用户。

**POST /auth/logout：**

功能：处理用户登出。

响应：ApiResponse（返回"Logout Successful"）。

实现：清除SecurityContext和会话，记录登出日志。

代码位置：com.example.restservice.auth.controller.AuthController

### 3.3 MySecurityService

作用：提供权限检查服务，验证用户对作业、提交、文件等资源的访问权限。

关键方法：

**isSubmissionOwner(Authentication, Long)：** 检查用户是否为提交的所有者。

**isAssignmentOwner(Authentication, Long)：** 检查用户是否为作业创建者。

**isAssignmentFileOwner(Authentication, Long)：** 检查用户是否为作业文件的所有者。

**isSubmissionFileOwner(Authentication, Long)：** 检查用户是否为提交文件的所有者。

**isTeacherByAssignment(Authentication, Long)：** 检查用户是否为作业所在课程的教师。

**isTeacherBySubmission(Authentication, Long)：** 检查用户是否为提交所在课程的教师。

**isTeacherBySubmissionFile(Authentication, Long)：** 检查用户是否为提交文件所在课程的教师。

**canCreateSubmission(Authentication, Submission)：** 验证用户是否能创建提交（检查用户身份、课程注册和截止日期）。

**canCreateAssignment(Authentication, Assignment)：** 验证用户是否能创建作业（检查用户身份、教师角色和截止日期）。

**inCourse(Authentication, long)：** 检查用户是否在指定课程中。

**inCourseByAssignment(Authentication, long)：** 检查用户是否在作业所在课程中。

**inCourseByAssignmentFile(Authentication, long)：** 检查用户是否在作业文件所在课程中。

**isAssignmentOwnerByAssignmentFile(Authentication, AssignmentFile)：** 检查用户是否为作业文件关联的作业创建者。

**isUser(Authentication, long)：** 检查是否为指定用户。

**isScheduleOwner(Authentication, long)：** 检查用户是否为日程的所有者。

实现：通过Authentication获取当前用户，结合JPA查询验证用户角色、课程关系和资源所有权。

代码位置：com.example.restservice.auth.service.MySecurityService

### 3.4 MyUserDetailsService

作用：实现Spring Security的UserDetailsService，用于加载用户认证信息。

关键方法：

**loadUserByUsername(String)：** 根据用户名从UserRepository查询用户，失败抛出UsernameNotFoundException。

实现：通过UserRepository查找用户，返回UserDetails对象，供Spring Security认证使用。

代码位置：com.example.restservice.auth.service.MyUserDetailsService

### 3.5 Oauth2LoginSuccessHandler

作用：处理OAuth2登录成功后的逻辑。

关键实现：

处理流程：

从OAuth2AuthenticationToken获取提供商（如Google）和用户ID（subject）。
        
检查OauthUserRepository中是否存在匹配的OauthUser记录。
        
若存在，直接认证关联用户；若不存在，创建新User和OauthUser，并保存。
        
保存认证信息到SecurityContext，重定向到前端仪表板（URL从frontend.url配置读取）。
    
用户创建：从OIDC用户信息提取邮箱和用户名，设置默认密码和Role.USER角色。
    
事务管理：使用@Transactional确保用户创建和关联操作的原子性。

代码位置：com.example.restservice.auth.oauth.Oauth2LoginSuccessHandler

## 4. API文档

API端点由多个控制器类实现。以下是主要控制器的功能概述：

### AssignmentController

作用：管理作业的创建、查询、更新和删除。

代码位置：com.example.restservice.controller.AssignmentController

API端点：

**POST /assignments**

功能：创建新作业。

权限：需要ROLE_ADMIN或通过MySecurityService.canCreateAssignment验证（用户为教师且在截止日期前）。

请求：Assignment对象（包含user和course引用）。

响应：ApiResponse（成功返回PostReturnData含新作业ID，失败返回错误）。

实现：设置用户和课程引用，保存作业，记录日志。

**GET /assignments?user-id={userId}**

功能：获取指定用户参与课程的所有作业。

权限：需要ROLE_ADMIN或通过MySecurityService.isUser验证（用户为本人）。
        
参数：user-id（用户ID）。

响应：ApiResponse（成功返回List<Assignment>，失败返回错误）。

实现：查询用户课程的作业，记录日志。

**GET /assignments/{id}**

功能：获取指定ID的作业。

权限：需要ROLE_ADMIN或通过MySecurityService.inCourseByAssignment验证（用户在作业课程中）。

参数：id（作业ID，路径参数）。

响应：ApiResponse（成功返回Assignment，失败返回错误）。

实现：查询作业，记录日志。

**GET /assignments?course-id={courseId}**

功能：获取指定课程的所有作业。

权限：需要ROLE_ADMIN或通过MySecurityService.inCourse验证（用户在课程中）。

参数：course-id（课程ID）。

响应：ApiResponse（成功返回List<Assignment>，失败返回错误）。

实现：查询课程作业，记录日志。

**PUT /assignments/{id}**

功能：更新指定ID的作业。

权限：需要ROLE_ADMIN或通过MySecurityService.isTeacherByAssignment验证（用户为课程教师）。

参数：id（作业ID，路径参数），Assignment（更新数据）。

响应：ApiResponse（成功返回更新后的Assignment，失败返回错误）。

实现：更新作业字段，保存并记录日志。

**DELETE /assignments/{id}**
        
功能：删除指定ID的作业。

权限：需要ROLE_ADMIN或通过MySecurityService.isAssignmentOwner验证（用户为作业创建者）。

参数：id（作业ID，路径参数）。

响应：ApiResponse（成功返回空响应，失败返回错误）。

实现：删除作业，记录日志。

### AssignmentFileController

作用：管理作业文件的创建、查询和删除。

代码位置：com.example.restservice.controller.AssignmentFileController

API端点：

**GET /assignment-files**

功能：获取所有作业文件。

权限：需要ROLE_ADMIN。

响应：ApiResponse（成功返回List<AssignmentFile>，失败返回错误）。

实现：查询所有作业文件，记录日志。

**GET /assignment-files/{id}**

功能：获取指定ID的作业文件。

权限：需要ROLE_ADMIN或通过MySecurityService.inCourseByAssignmentFile验证（用户在作业文件课程中）。

参数：id（作业文件ID，路径参数）。

响应：ApiResponse（成功返回AssignmentFile，失败返回"AssignmentFile not found"）。

实现：查询作业文件，记录日志。

**GET /assignment-files?assignment-id={assignmentId}**

功能：获取指定作业的所有作业文件。

权限：需要ROLE_ADMIN或通过MySecurityService.inCourseByAssignment验证（用户在作业课程中）。

参数：assignment-id（作业ID）。

响应：ApiResponse（成功返回List<AssignmentFile>，失败返回错误）。

实现：查询作业相关文件，记录日志。

**POST /assignment-files**

功能：创建新作业文件。

权限：需要ROLE_ADMIN或通过MySecurityService.isAssignmentOwnerByAssignmentFile验证（用户为作业创建者）。

请求：AssignmentFile（包含assignment和file引用）。

响应：ApiResponse（成功返回PostReturnData含新作业文件ID，失败返回错误，如记录已存在或引用无效）。

实现：验证作业和文件存在，检查记录重复，保存并记录日志。

**DELETE /assignment-files/{id}**

功能：删除指定ID的作业文件。

权限：需要ROLE_ADMIN或通过MySecurityService.isAssignmentFileOwner验证（用户为作业文件关联作业的创建者）。

参数：id（作业文件ID，路径参数）。

响应：ApiResponse（成功返回空响应，失败返回"AssignmentFile not found"）。

实现：删除作业文件，记录日志。

### CourseController

作用：管理课程的创建、查询、更新和删除。

代码位置：com.example.restservice.controller.CourseController

API端点：

**POST /courses**

功能：创建新课程。

权限：需要ROLE_ADMIN。

请求：Course对象。

响应：ApiResponse（成功返回PostReturnData含新课程ID，失败返回错误）。

实现：保存课程，记录日志。

**GET /courses**

功能：获取所有课程。

权限：无限制。

响应：ApiResponse（成功返回List<Course>，失败返回错误）。

实现：查询所有课程，记录日志。

**GET /courses/{id}**

功能：获取指定ID的课程。

权限：无限制。

参数：id（课程ID，路径参数）。

响应：ApiResponse（成功返回Course，失败返回错误）。

实现：查询课程，记录日志。

**PUT /courses/{id}**

功能：更新指定ID的课程。

权限：需要ROLE_ADMIN。

参数：id（课程ID，路径参数），Course（更新数据）。

响应：ApiResponse（成功返回更新后的Course，失败返回错误）。

实现：更新课程字段，保存并记录日志。

**DELETE /courses/{id}**

功能：删除指定ID的课程。

权限：需要ROLE_ADMIN。

参数：id（课程ID，路径参数）。

响应：ApiResponse（成功返回空响应，失败返回错误）。

实现：删除课程，记录日志。

### CourseUserController

作用：管理课程与用户的关系（如添加用户到课程、查询用户课程关系）。

代码位置：com.example.restservice.controller.CourseUserController

API端点：

**POST /course-users**

功能：将用户添加到课程。

权限：需要ROLE_ADMIN。

请求：CourseUser（包含course和user引用及角色）。

响应：ApiResponse（成功返回PostReturnData含新记录ID，失败返回错误，如课程/用户不存在或重复注册）。

实现：验证课程和用户存在，检查重复记录，保存并记录日志。

**GET /course-users**

功能：获取所有课程用户关系。

权限：无限制。

响应：ApiResponse（成功返回List<CourseUser>，失败返回错误）。

实现：查询所有课程用户关系，记录日志。

**GET /course-users/{id}**

功能：获取指定ID的课程用户关系。

权限：无限制。

参数：id（记录ID，路径参数）。

响应：ApiResponse（成功返回CourseUser，失败返回"CourseUser not found"）。

实现：查询记录，记录日志。

**GET /course-users?course-id={courseId}**

功能：获取指定课程的所有用户。

权限：无限制。

参数：course-id（课程ID）。

响应：ApiResponse（成功返回List<CourseUser>，失败返回"Course not found"）。

实现：验证课程存在，查询用户，记录日志。

**GET /course-users?user-id={userId}**

功能：获取指定用户的所有课程。

权限：无限制。

参数：user-id（用户ID）。

响应：ApiResponse（成功返回List<CourseUser>，失败返回"User not found"）。

实现：验证用户存在，查询课程，记录日志。

**GET /course-users?user-id={userId}&course-id={courseId}**

功能：获取指定用户和课程的课程用户关系。

权限：无限制。

参数：user-id（用户ID），course-id（课程ID）。

响应：ApiResponse（成功返回Optional<CourseUser>，失败返回"User or Course not found"）。

实现：验证用户和课程存在，查询记录，记录日志。

**PUT /course-users/{id}**

功能：更新课程用户关系的角色。

权限：需要ROLE_ADMIN。

参数：id（记录ID，路径参数），CourseUser（更新角色数据）。

响应：ApiResponse（成功返回更新后的CourseUser，失败返回"CourseUser not found"）。

实现：仅更新角色字段，保存并记录日志。

**DELETE /course-users/{id}**

功能：删除课程用户关系。

权限：需要ROLE_ADMIN。

参数：id（记录ID，路径参数）。

响应：ApiResponse（成功返回"CourseUser deleted successfully"，失败返回"CourseUser not found"）。

实现：删除记录，记录日志。

### FileController

作用：管理文件的上传、查询、下载、OCR处理和删除。

代码位置：com.example.restservice.controller.FileController

API端点：

**POST /files**

功能：上传文件。

权限：无限制。

请求：multipart/form-data（含file字段）。

响应：ApiResponse（成功返回PostReturnData含新文件ID，失败返回错误，如文件为空或上传失败）。

实现：验证文件名，生成唯一文件名，保存到本地和阿里云OSS，记录日志。

**GET /files**

功能：获取所有文件元信息或以ZIP下载。

权限：需要ROLE_ADMIN。

参数：download（布尔值，默认为false）。

响应：

download=false：ApiResponse（返回List<File>）。

download=true：ZIP文件流（Content-Type: application/octet-stream）。

实现：查询文件列表或生成ZIP文件，记录日志。

**GET /files/{id}**

功能：获取文件元信息或下载文件。

权限：无限制。

参数：id（文件ID，路径参数），metadata（布尔值，默认为false）。

响应：

metadata=true：ApiResponse（返回File）。

metadata=false：文件流（Content-Type: application/octet-stream）。

实现：查询文件或从OSS下载内容，记录日志。

**GET /files/{id}/ocr**

功能：获取文件的OCR处理结果。

权限：无限制。

参数：id（文件ID，路径参数）。

响应：ApiResponse（返回OCR处理后的PDF或错误）。

实现：调用OCRService.downloadFileFromOcr，记录日志。

**GET /files/{id}/pdf**

功能：获取文件的PDF版本（原文件为PDF或转换后）。

权限：无限制。

参数：id（文件ID，路径参数）。

响应：PDF文件流（Content-Type: application/pdf）或错误。

实现：检查文件是否为PDF或转换到PDF，记录日志。

**DELETE /files/{id}**

功能：删除指定文件。

权限：无限制。

参数：id（文件ID，路径参数）。

响应：ApiResponse（成功返回空响应，失败返回"文件未找到"或删除错误）。

实现：从OSS和数据库删除文件，记录日志。

### LogController

作用：管理用户日志的查询。

代码位置：com.example.restservice.controller.LogController

API端点：

**GET /logs/user/{userId}**

功能：获取指定用户的所有日志。

权限：需要ROLE_ADMIN或用户为本人（userId == authentication.principal.id）。

参数：userId（用户ID，路径参数）。

响应：ApiResponse（成功返回List<UserLog>，失败返回错误信息）。

实现：查询用户日志，记录请求日志。

**GET /logs/all**

功能：获取所有日志。

权限：需要ROLE_ADMIN。

响应：ApiResponse（成功返回List<UserLog>，失败返回错误）。

实现：调用LogService.getAllLogs，记录日志。

**GET /logs/all/range?start={startTime}&end={endTime}**

功能：获取指定时间范围内的所有日志。

权限：需要ROLE_ADMIN。

参数：start（开始时间，格式yyyy-MM-dd'T'HH:mm:ss），end（结束时间，格式同上）。

响应：ApiResponse（成功返回List<UserLog>，失败返回错误）。

实现：调用LogService.getLogsByTimeRange，记录日志。

**GET /logs/user/{userId}/range?start={startTime}&end={endTime}**

功能：获取指定用户在指定时间范围内的日志。

权限：需要ROLE_ADMIN或用户为本人（userId == authentication.principal.id）。

参数：userId（用户ID，路径参数），start（开始时间，格式yyyy-MM-dd'T'HH:mm:ss），end（结束时间，格式同上）。

响应：ApiResponse（成功返回List<UserLog>，失败返回错误，如时间格式错误）。

实现：调用LogService.getLogsByUserAndTimeRange，记录日志。

### ScheduleController

作用：管理日程的创建、查询、更新和删除。

代码位置：com.example.restservice.controller.ScheduleController

API端点：

**POST /schedules**

功能：创建新日程。

权限：需要ROLE_ADMIN或用户为日程所有者（schedule.getUser().getId() == authentication.principal.id）。

请求：Schedule（包含user引用）。

响应：ApiResponse（成功返回PostReturnData含新日程ID，失败返回错误，如用户不存在）。

实现：验证用户存在，设置创建时间，保存日程，记录日志。

**GET /schedules**

功能：获取所有日程。

权限：需要ROLE_ADMIN。

响应：ApiResponse（成功返回List<Schedule>，失败返回错误）。

实现：查询所有日程，记录日志。

**GET /schedules/{id}**

功能：获取指定ID的日程。

权限：需要ROLE_ADMIN或通过MySecurityService.isScheduleOwner验证（用户为日程所有者）。

参数：id（日程ID，路径参数）。

响应：ApiResponse（成功返回Schedule，失败返回"No such schedule"）。

实现：查询日程，记录日志。

**PUT /schedules/{id}**

功能：更新指定ID的日程。

权限：需要ROLE_ADMIN或通过MySecurityService.isScheduleOwner验证（用户为日程所有者）。

参数：id（日程ID，路径参数），Schedule（更新数据）。

响应：ApiResponse（成功返回更新后的Schedule，失败返回错误，如日程或用户不存在）。

实现：更新日程字段，验证并更新用户，保存并记录日志。

**DELETE /schedules/{id}**

功能：删除指定ID的日程。

权限：需要ROLE_ADMIN或通过MySecurityService.isScheduleOwner验证（用户为日程所有者）。

参数：id（日程ID，路径参数）。

响应：ApiResponse（成功返回空响应，失败返回"No such schedule"）。

实现：删除日程，记录日志。

### SubmissionController

作用：管理作业提交的创建、查询、更新、删除及评判。

代码位置：com.example.restservice.controller.SubmissionController

API端点：

**POST /submissions**

功能：创建新提交。

权限：需要ROLE_ADMIN或通过MySecurityService.canCreateSubmission验证（用户为本人、在课程中且未过截止日期）。

请求：Submission（包含user和assignment引用）。

响应：ApiResponse（成功返回PostReturnData含新提交ID，失败返回错误）。

实现：设置用户和作业引用，保存提交，记录日志。

**GET /submissions**

功能：获取所有提交。

权限：需要ROLE_ADMIN。

响应：ApiResponse（成功返回List<Submission>，失败返回错误）。

实现：查询所有提交，记录日志。

**GET /submissions?assignment-id={assignmentId}**

功能：获取指定作业的所有提交。

权限：需要ROLE_ADMIN或通过MySecurityService.isTeacherByAssignment验证（用户为课程教师）。

参数：assignment-id（作业ID）。

响应：ApiResponse（成功返回List<Submission>，失败返回错误）。

实现：查询作业提交，记录日志。

**GET /submissions?assignment-id={assignmentId}&latest**

功能：获取指定作业每个用户的最新提交。

权限：需要ROLE_ADMIN或通过MySecurityService.isTeacherByAssignment验证（用户为课程教师）。

参数：assignment-id（作业ID），latest（标识获取最新提交）。

响应：ApiResponse（成功返回List<Submission>，失败返回错误）。

实现：按用户分组获取最新提交，记录日志。

**GET /submissions?user-id={userId}&assignment-id={assignmentId}**

功能：获取指定用户和作业的提交。

 权限：需要ROLE_ADMIN、用户为本人（userId == authentication.principal.id）或通过MySecurityService.isTeacherByAssignment验证（用户为课程教师）。

参数：user-id（用户ID），assignment-id（作业ID）。

响应：ApiResponse（成功返回List<Submission>，失败返回错误）。

实现：查询用户和作业的提交，记录日志。

**GET /submissions?user-id={userId}&assignment-id={assignmentId}&sort=submit-time&order=desc**

功能：获取指定用户和作业的最新提交。

权限：需要ROLE_ADMIN、用户为本人（userId == authentication.principal.id）或通过MySecurityService.isTeacherByAssignment验证（用户为课程教师）。

参数：user-id（用户ID），assignment-id（作业ID），sort=submit-time，order=desc。

响应：ApiResponse（成功返回Submission或null，失败返回错误）。

实现：查询最新提交，记录日志。

**GET /submissions/{id}**

功能：获取指定ID的提交。

权限：需要ROLE_ADMIN、通过MySecurityService.isSubmissionOwner验证（用户为提交者）或通过MySecurityService.isTeacherBySubmission验证（用户为课程教师）。

参数：id（提交ID，路径参数）。

响应：ApiResponse（成功返回Submission，失败返回错误）。

实现：查询提交，记录日志。

**PUT /submissions/{id}**

功能：更新指定ID的提交。

权限：需要ROLE_ADMIN或通过MySecurityService.isTeacherBySubmission验证（用户为课程教师）。

参数：id（提交ID，路径参数），Submission（更新数据）。

响应：ApiResponse（成功返回更新后的Submission，失败返回错误）。

实现：更新提交字段，保存并记录日志。

**DELETE /submissions/{id}**

功能：删除指定ID的提交。

权限：需要ROLE_ADMIN或通过MySecurityService.isSubmissionOwner验证（用户为提交者）。

参数：id（提交ID，路径参数）。

响应：ApiResponse（成功返回空响应，失败返回错误）。

实现：删除提交，记录日志。

**GET /submissions/{id}/judges**

功能：获取指定提交的评判结果。

权限：无明确权限控制（依赖服务层限制）。

参数：id（提交ID，路径参数）。

响应：ApiResponse（成功返回List<Map<String, Object>>评判结果，失败返回错误）。

实现：调用JudgeService.getBatchResults，记录日志。

**POST /submissions/{id}/judges**

功能：为指定提交触发评判（仅代码类型作业）。

权限：无明确权限控制（依赖服务层限制）。

参数：id（提交ID，路径参数）。

响应：ApiResponse（成功返回空响应，失败返回错误）。

实现：若作业类型为CODE，调用JudgeService.batchJudge，记录日志。

### SubmissionFileController

作用：管理提交文件的创建、查询和删除。

代码位置：com.example.restservice.controller.SubmissionFileController

API端点：

**GET /submission-files**

功能：获取所有提交文件。

权限：需要ROLE_ADMIN。

响应：ApiResponse（成功返回List<SubmissionFile>，失败返回错误）。

实现：查询所有提交文件，记录日志。

**GET /submission-files/{id}**

功能：获取指定ID的提交文件。

权限：需要ROLE_ADMIN、通过MySecurityService.isSubmissionFileOwner验证（用户为提交者）或通过MySecurityService.isTeacherBySubmissionFile验证（用户为课程教师）。

参数：id（提交文件ID，路径参数）。

响应：ApiResponse（成功返回SubmissionFile，失败返回"SubmissionFile not found"）。

实现：查询提交文件，记录日志。
    
**GET /submission-files?submission-id={submissionId}**

功能：获取指定提交的所有提交文件。

权限：需要ROLE_ADMIN、通过MySecurityService.isSubmissionOwner验证（用户为提交者）或通过MySecurityService.isTeacherBySubmission验证（用户为课程教师）。

参数：submission-id（提交ID）。

响应：ApiResponse（成功返回List<SubmissionFile>，失败返回错误）。

实现：查询提交相关文件，记录日志。

**POST /submission-files**

功能：创建新提交文件。

权限：需要ROLE_ADMIN或通过MySecurityService.isSubmissionOwner验证（用户为提交者）。

请求：SubmissionFile（包含submission和file引用）。

响应：ApiResponse（成功返回PostReturnData含新提交文件ID，失败返回错误，如记录已存在、提交或文件不存在）。

实现：验证提交和文件存在，检查重复记录，若作业需OCR且文件为支持格式（.jpeg/.jpg/.png/.pdf），发送至OCR服务，保存并记录日志。

**DELETE /submission-files/{id}**

功能：删除指定ID的提交文件。

权限：需要ROLE_ADMIN或通过MySecurityService.isSubmissionFileOwner验证（用户为提交者）。

参数：id（提交文件ID，路径参数）。

响应：ApiResponse（成功返回空响应，失败返回"SubmissionFile not found"）。

实现：删除提交文件，记录日志。

### TestCaseController

作用：管理测试用例的创建、查询、更新和删除。

代码位置：com.example.restservice.controller.TestCaseController

API端点：

**POST /test-cases**

功能：创建新测试用例。

权限：无明确权限控制（建议添加管理员或教师权限）。

请求：TestCase（包含assignment引用）。

响应：ApiResponse（成功返回PostReturnData含新测试用例ID，失败返回错误）。

实现：设置作业引用，保存测试用例，记录日志。

**GET /test-cases**

功能：获取所有测试用例。

权限：无明确权限控制（建议添加管理员权限）。

响应：ApiResponse（成功返回List<TestCase>，失败返回错误）。

实现：查询所有测试用例，记录日志。

**GET /test-cases?assignment-id={assignmentId}**

功能：获取指定作业的测试用例。

权限：无明确权限控制（建议添加管理员或教师权限）。

参数：assignment-id（作业ID）。

响应：ApiResponse（成功返回List<TestCase>，失败返回错误）。

实现：查询作业相关测试用例，记录日志。

**GET /test-cases/{id}**

功能：获取指定ID的测试用例。

权限：无明确权限控制（建议添加管理员或教师权限）。

参数：id（测试用例ID，路径参数）。

响应：ApiResponse（成功返回TestCase，失败返回错误）。

实现：查询测试用例，记录日志。

**PUT /test-cases/{id}**

功能：更新指定ID的测试用例。

权限：无明确权限控制（建议添加管理员或教师权限）。

参数：id（测试用例ID，路径参数），TestCase（更新数据）。

响应：ApiResponse（成功返回更新后的Assignment，失败返回错误）。

实现：更新测试用例字段，保存作业，记录日志。

**DELETE /test-cases/{id}**

功能：删除指定ID的测试用例。

权限：无明确权限控制（建议添加管理员或教师权限）。

参数：id（测试用例ID，路径参数）。

响应：ApiResponse（成功返回空响应，失败返回错误）。

实现：删除测试用例，记录日志。

### UserController

作用：管理用户的创建、查询、更新和删除。

代码位置：com.example.restservice.controller.UserController

API端点：

**POST /users**

功能：创建新用户。

权限：需要ROLE_ADMIN。

请求：User（包含用户名、邮箱、密码、角色等）。

响应：ApiResponse（成功返回PostReturnData含新用户ID，失败返回错误）。

实现：加密密码，保存用户，记录日志。

**POST /users/batch**

功能：通过CSV文件批量创建用户。

权限：需要ROLE_ADMIN。

请求：multipart/form-data（含file字段，CSV格式，包含username,email,password,role）。

响应：ApiResponse（成功返回List<PostReturnData>含新用户ID列表，失败返回错误，如文件格式错误或无有效用户）。

实现：验证文件类型，解析CSV，创建并批量保存用户，记录日志。

**GET /users**

功能：获取所有用户。

权限：需要ROLE_ADMIN。

响应：ApiResponse（成功返回List<User>，失败返回错误）。

实现：查询所有用户，记录日志。

**GET /users/{id}**

功能：获取指定ID的用户。

权限：无限制。

参数：id（用户ID，路径参数）。

响应：ApiResponse（成功返回User，失败返回错误）。

实现：查询用户，记录日志。

**PUT /users/{id}**

功能：更新指定ID的用户。

权限：需要ROLE_ADMIN或用户为本人（id == authentication.
principal.id）。

参数：id（用户ID，路径参数），User（更新数据）。

响应：ApiResponse（成功返回更新后的User，失败返回错误）。

实现：加密新密码（如提供），更新用户字段，保存并记录日志。

**DELETE /users/{id}**

功能：删除指定ID的用户。

权限：需要ROLE_ADMIN。

参数：id（用户ID，路径参数）。

响应：ApiResponse（成功返回空响应，失败返回错误）。

实现：删除用户，记录日志。

## 5. 数据模型

数据模型定义了数据库表和对应的Java类。主要实体包括：

## 3. 数据模型

以下是系统中主要实体的数据模型描述，包括字段、类型、关系和用途。所有实体使用JPA注解定义，存储在关系型数据库中。

### 3.1 Assignment

**表名**：`assignments`

**作用**：表示课程中的作业，支持代码和非代码类型，可关联测试用例、提交和文件。

**字段**：

| 字段名                     | 类型                     | 描述                                   | 约束                |
|----------------------------|--------------------------|----------------------------------------|---------------------|
| `id`                       | Long                    | 主键，作业ID                          | 自增                |
| `user`                     | User                    | 创建作业的用户                        | 外键                |
| `course`                   | Course                  | 所属课程                              | 外键                |
| `dueDate`                  | LocalDateTime           | 作业截止时间                          |                     |
| `fullMark`                 | Integer                 | 作业满分                              |                     |
| `publishTime`              | LocalDateTime           | 作业发布时间（对学生可见）            |                     |
| `title`                    | String                  | 作业标题                              |                     |
| `description`              | String                  | 作业描述                              |                     |
| `maxAttempts`              | Integer                 | 最大提交次数                          |                     |
| `type`                     | AssignmentType (Enum)   | 作业类型（CODE/NON_CODE）             |                     |
| `createTime`               | LocalDateTime           | 作业创建时间                          |                     |
| `needOCR`                  | Boolean                 | 是否需要OCR处理，默认为`false`        | 非空                |
| `programmingLanguages`     | Set<ProgrammingLanguage>| 支持的编程语言（仅代码作业）          | 集合                |
| `testCases`                | List<TestCase>          | 关联的测试用例                        | 一对多，级联删除    |
| `assignmentFiles`          | Set<AssignmentFile>     | 关联的作业文件                        | 一对多，级联删除    |
| `submissions`              | Set<Submission>         | 关联的学生提交                        | 一对多，级联删除    |

**关系**：
- `user`：多对一，作业由一个用户（通常为教师）创建。
- `course`：多对一，作业属于一个课程。
- `testCases`：一对多，作业可有多个测试用例。
- `assignmentFiles`：一对多，作业可关联多个文件。
- `submissions`：一对多，作业可有多个学生提交。

**方法**：
- `updateWithoutId(Assignment)`：更新除ID外的字段。

---

### 3.2 AssignmentFile

**表名**：`assignments_files`

**作用**：表示作业与文件的关联关系（如作业描述附件）。

**字段**：

| 字段名       | 类型       | 描述               | 约束                     |
|--------------|------------|--------------------|--------------------------|
| `id`         | Long       | 主键，关联ID       | 自增                     |
| `assignment` | Assignment | 关联的作业         | 外键                     |
| `file`       | File       | 关联的文件         | 外键                     |

**约束**：
- 唯一约束：`assignment_id`和`file_id`组合唯一。

**关系**：
- `assignment`：多对一，一个作业文件属于一个作业。
- `file`：多对一，一个作业文件对应一个文件。

---

### 3.3 Course

**表名**：`courses`

**作用**：表示课程，可关联用户和作业。

**字段**：

| 字段名        | 类型               | 描述               | 约束                  |
|---------------|--------------------|--------------------|-----------------------|
| `id`          | Long               | 主键，课程ID       | 自增                  |
| `name`        | String             | 课程名称           |                       |
| `courseUsers` | List<CourseUser>   | 课程用户关系       | 一对多，级联删除      |
| `assignments` | Set<Assignment>    | 课程中的作业       | 一对多，级联删除      |

**方法**：
- `updateWithoutId(Course)`：更新除ID外的字段（仅`name`）。

**关系**：
- `courseUsers`：一对多，课程可有多个用户（如学生、教师）。
- `assignments`：一对多，课程可有多个作业。

---

### 3.4 CourseUser

**表名**：`courses_users`

**作用**：表示课程与用户的关系，定义用户在课程中的角色。

**字段**：

| 字段名   | 类型           | 描述                     | 约束                     |
|----------|----------------|--------------------------|--------------------------|
| `id`     | Long           | 主键，关系ID             | 自增                     |
| `course` | Course         | 关联的课程               | 外键                     |
| `user`   | User           | 关联的用户               | 外键                     |
| `role`   | RoleInCourse   | 用户在课程中的角色       | 非空，枚举（TEACHER/STUDENT） |

**约束**：
- 唯一约束：`course_id`和`user_id`组合唯一。

**关系**：
- `course`：多对一，关系属于一个课程。
- `user`：多对一，关系属于一个用户。

---

### 3.5 File

**表名**：`files`

**作用**：表示系统中上传的文件，可关联作业或提交。

**字段**：

| 字段名            | 类型                | 描述                     | 约束                     |
|-------------------|---------------------|--------------------------|--------------------------|
| `id`              | Long                | 主键，文件ID             | 自增                     |
| `originalName`    | String              | 文件原始名称             | 非空                     |
| `fileName`        | String              | 重命名后的唯一文件名     | 非空，唯一               |
| `assignmentFiles` | Set<AssignmentFile> | 关联的作业文件关系       | 一对多，级联删除         |
| `submissionsFiles`| Set<SubmissionFile> | 关联的提交文件关系       | 一对多，级联删除         |

**关系**：
- `assignmentFiles`：一对多，文件可用于多个作业。
- `submissionsFiles`：一对多，文件可用于多个提交。

---

### 3.6 OauthUser

**表名**：`oauth_users`

**作用**：表示用户的OAuth第三方登录信息。

**字段**：

| 字段名      | 类型            | 描述                     | 约束                     |
|-------------|-----------------|--------------------------|--------------------------|
| `id`        | Long            | 主键，OAuth记录ID       | 自增                     |
| `provider`  | String          | 第三方认证提供者（如Google）|                         |
| `subject`   | String          | 第三方用户ID             |                         |
| `createTime`| LocalDateTime   | 创建时间                 |                         |
| `user`      | User            | 关联的用户               | 外键，非空               |

**关系**：
- `user`：多对一，OAuth记录属于一个用户。

---

### 3.7 Schedule

**表名**：`schedules`

**作用**：表示用户的个人日程。

**字段**：

| 字段名        | 类型            | 描述                     | 约束                     |
|---------------|-----------------|--------------------------|--------------------------|
| `id`          | Long            | 主键，日程ID             | 自增                     |
| `user`        | User            | 关联的用户               | 外键                     |
| `createTime`  | LocalDateTime   | 创建时间                 |                         |
| `time`        | LocalDateTime   | 日程设定时间             |                         |
| `title`       | String          | 日程标题                 |                         |
| `description` | String          | 日程描述                 |                         |

**方法**：
- `updateWithoutId(Schedule)`：更新除ID外的字段。

**关系**：
- `user`：多对一，日程属于一个用户。

---

### 3.8 Submission

**表名**：`submissions`

**作用**：表示学生对作业的提交，可包含文件、代码和评判结果。

**字段**：

| 字段名            | 类型                     | 描述                         | 约束                     |
|-------------------|--------------------------|------------------------------|--------------------------|
| `id`              | Long                     | 主键，提交ID                 | 自增                     |
| `assignment`      | Assignment               | 关联的作业                   | 外键                     |
| `user`            | User                     | 提交的用户                   | 外键                     |
| `submitTime`      | LocalDateTime            | 提交时间                     |                         |
| `score`           | Float                    | 分数                         |                         |
| `scoreVisible`    | Boolean                  | 分数是否对学生可见           |                         |
| `programmingLanguage` | ProgrammingLanguage   | 使用的编程语言（仅代码作业） |                         |
| `judgeIds`        | List<String>             | 评判ID列表                   | 集合                     |
| `submissionFiles` | Set<SubmissionFile>      | 关联的提交文件               | 一对多，级联删除         |

**方法**：
- `updateWithoutId(Submission)`：更新除ID外的字段。

**关系**：
- `assignment`：多对一，提交属于一个作业。
- `user`：多对一，提交由一个用户创建。
- `submissionFiles`：一对多，提交可包含多个文件。

---

### 3.9 SubmissionFile

**表名**：`submission_files`

**作用**：表示提交与文件的关联关系（如学生上传的答案文件）。

**字段**：

| 字段名       | 类型       | 描述               | 约束                     |
|--------------|------------|--------------------|--------------------------|
| `id`         | Long       | 主键，关联ID       | 自增                     |
| `submission` | Submission | 关联的提交         | 外键                     |
| `file`       | File       | 关联的文件         | 外键                     |

**约束**：
- 唯一约束：`submission_id`和`file_id`组合唯一。

**关系**：
- `submission`：多对一，一个提交文件属于一个提交。
- `file`：多对一，一个提交文件对应一个文件。

---

### 3.10 TestCase

**表名**：`test_cases`

**作用**：表示代码作业的测试用例，定义运行环境和预期输出。

**字段**：

| 字段名                             | 类型            | 描述                                   | 约束                     |
|------------------------------------|-----------------|----------------------------------------|--------------------------|
| `id`                               | Long            | 主键，测试用例ID                       | 自增                     |
| `assignment`                       | Assignment      | 关联的作业                             | 外键，非空               |
| `compilerOptions`                  | String          | 编译选项                               |                         |
| `commandLineArguments`             | String          | 命令行参数                             |                         |
| `cpuTimeLimit`                     | Float           | CPU时间限制（秒）                      |                         |
| `cpuExtraTime`                     | Float           | 额外CPU时间（秒）                      |                         |
| `wallTimeLimit`                    | Float           | 墙钟时间限制（秒）                     |                         |
| `memoryLimit`                      | Float           | 内存限制（MB）                         |                         |
| `stackLimit`                       | Integer         | 栈大小限制（MB）                       |                         |
| `maxProcessesAndOrThreads`         | Integer         | 最大进程/线程数                        |                         |
| `enablePerProcessAndThreadTimeLimit`| Boolean         | 是否启用每进程/线程时间限制            |                         |
| `enablePerProcessAndThreadMemoryLimit`| Boolean      | 是否启用每进程/线程内存限制            |                         |
| `maxFileSize`                      | Integer         | 最大文件大小（KB）                     |                         |
| `redirectStderrToStdout`           | Boolean         | 是否将stderr重定向到stdout             |                         |
| `enableNetwork`                    | Boolean         | 是否允许网络访问                       |                         |
| `numberOfRuns`                     | Integer         | 测试运行次数                           |                         |
| `stdin`                            | String          | 标准输入                               |                         |
| `expectedOutput`                   | String          | 预期输出                               |                         |

**方法**：
- `updateWithoutId(TestCase)`：更新除ID外的字段。

**关系**：
- `assignment`：多对一，测试用例属于一个作业。

---

### 3.11 User

**表名**：`users`

**作用**：表示系统用户，支持Spring Security认证，包含角色和主题设置。

**字段**：

| 字段名        | 类型               | 描述                          | 约束                     |
|---------------|--------------------|-------------------------------|--------------------------|
| `id`          | Long               | 主键，用户ID                  | 自增                     |
| `email`       | String             | 邮箱                          | 非空，唯一               |
| `username`    | String             | 用户名                        | 非空，唯一               |
| `password`    | String             | 加密密码                      | 非空                     |
| `role`        | Role (Enum)        | 用户角色（`ADMIN`, `USER`）        | 非空                 |
| `userTheme`   | UserTheme (Enum)   | 用户界面主题，默认为`AUTO`      | 非空                     |
| `courseUsers` | Set<CourseUser>    | 课程用户关系                  | 一对多，级联删除         |
| `submissions` | Set<Submission>    | 用户提交                      | 一对多，级联删除         |
| `assignments` | Set<Assignment>`    | 用户创建的作业                | 一对多，级联删除         |
| `oauthUsers`  | List<OauthUser>    | OAuth登录信息                 | 一对多，级联删除         |
| `userLogs`    | Set<UserLog>       | 用户日志                      | 一对多，级联删除         |
| `schedules`   | Set<Schedule>      | 用户日程                      | 一对多，级联删除         |

**方法**：
- `updateWithoutId(User)`：更新除ID外的字段。
- Spring Security 方法：`getAuthorities`, `getPassword`, `getUsername`, 等。

**关系**：
- `courseUsers`：一对多，用户可以参与多个课程。
- `submissions`：一对多，用户可以有多个提交。
- `assignments`：一对多，用户可以创建多个作业。
- `oauthUsers`：一对多，用户可以有多个OAuth登录记录。
- `userLogs`：一对多，用户可以有多个日志。
- `schedules`：一对多，用户可以有多个日程。

---

### 3.12 UserLog

**表名**：`users-logs`

**作用**：记录用户操作日志。

| 字段名 | 类型 | 描述 | 约束 |
|----------|-----------------|--------------------------|--------------------------|
| `id`     | Long            | 主键，日志ID | 自增 |
| `user_id`   | User            | 用户ID | 外键）|
| `logs`   | String          | 日志内容 | |
| `time`  | LocalDateTime | 日志记录时间 | |

**关系**：
- `user`：多对一，日志属于一个用户。

</span>