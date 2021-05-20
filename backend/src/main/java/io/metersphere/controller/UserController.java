package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.User;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.member.AddMemberRequest;
import io.metersphere.controller.request.member.EditPassWordRequest;
import io.metersphere.controller.request.member.QueryMemberRequest;
import io.metersphere.controller.request.member.UserRequest;
import io.metersphere.controller.request.organization.AddOrgMemberRequest;
import io.metersphere.controller.request.organization.QueryOrgMemberRequest;
import io.metersphere.controller.request.resourcepool.UserBatchProcessRequest;
import io.metersphere.dto.*;
import io.metersphere.excel.domain.ExcelResponse;
import io.metersphere.i18n.Translator;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.service.CheckPermissionService;
import io.metersphere.service.OrganizationService;
import io.metersphere.service.UserService;
import io.metersphere.service.WorkspaceService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("user")
@RestController
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private OrganizationService organizationService;
    @Resource
    private WorkspaceService workspaceService;
    @Resource
    private CheckPermissionService checkPermissionService;

    @PostMapping("/special/add")
    @RequiresRoles(RoleConstants.ADMIN)
    @MsAuditLog(module = "system_user", type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#user.id)", msClass = UserService.class)
    public UserDTO insertUser(@RequestBody UserRequest user) {
        return userService.insert(user);
    }

    @PostMapping("/special/list/{goPage}/{pageSize}")
    @RequiresRoles(RoleConstants.ADMIN)
    public Pager<List<User>> getUserList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody io.metersphere.controller.request.UserRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, userService.getUserListWithRequest(request));
    }

    @GetMapping("/special/user/role/{userId}")
    @RequiresRoles(RoleConstants.ADMIN)
    public UserRoleDTO getUserRole(@PathVariable("userId") String userId) {
        return userService.getUserRole(userId);
    }

    @GetMapping("/special/delete/{userId}")
    @RequiresRoles(RoleConstants.ADMIN)
    @MsAuditLog(module = "system_user", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#userId)", msClass = UserService.class)
    public void deleteUser(@PathVariable(value = "userId") String userId) {
        userService.deleteUser(userId);
        // 踢掉在线用户
        SessionUtils.kickOutUser(userId);
    }

    @PostMapping("/special/update")
    @RequiresRoles(RoleConstants.ADMIN)
    @MsAuditLog(module = "system_user", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#user.id)", content = "#msClass.getLogDetails(#user.id)", msClass = UserService.class)
    public void updateUser(@RequestBody UserRequest user) {
        userService.updateUserRole(user);
    }

    @PostMapping("/special/update_status")
    @RequiresRoles(RoleConstants.ADMIN)
    @MsAuditLog(module = "system_user", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#user.id)", content = "#msClass.getLogDetails(#user.id)", msClass = UserService.class)
    public void updateStatus(@RequestBody User user) {
        userService.updateUser(user);
    }

    @PostMapping("/special/ws/member/list/{goPage}/{pageSize}")
    @RequiresRoles(RoleConstants.ADMIN)
    public Pager<List<User>> getMemberListByAdmin(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryMemberRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, userService.getMemberList(request));
    }

    @PostMapping("/special/ws/member/list/all")
    @RequiresRoles(RoleConstants.ADMIN)
    public List<User> getMemberListByAdmin(@RequestBody QueryMemberRequest request) {
        return userService.getMemberList(request);
    }

    @PostMapping("/special/ws/member/add")
    @RequiresRoles(RoleConstants.ADMIN)
    @MsAuditLog(module = "workspace_member", type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#request.id)", msClass = WorkspaceService.class)
    public void addMemberByAdmin(@RequestBody AddMemberRequest request) {
        userService.addMember(request);
    }

    @GetMapping("/special/ws/member/delete/{workspaceId}/{userId}")
    @RequiresRoles(RoleConstants.ADMIN)
    @MsAuditLog(module = "workspace_member", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#userId)", msClass = UserService.class)
    public void deleteMemberByAdmin(@PathVariable String workspaceId, @PathVariable String userId) {
        userService.deleteMember(workspaceId, userId);
    }

    @PostMapping("/special/org/member/add")
    @RequiresRoles(RoleConstants.ADMIN)
    @MsAuditLog(module = "organization_member", type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#request.id)", msClass = OrganizationService.class)
    public void addOrganizationMemberByAdmin(@RequestBody AddOrgMemberRequest request) {
        userService.addOrganizationMember(request);
    }

    @GetMapping("/special/org/member/delete/{organizationId}/{userId}")
    @RequiresRoles(RoleConstants.ADMIN)
    @MsAuditLog(module = "organization_member", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#userId)", msClass = UserService.class)
    public void delOrganizationMemberByAdmin(@PathVariable String organizationId, @PathVariable String userId) {
        userService.delOrganizationMember(organizationId, userId);
    }

    @PostMapping("/special/org/member/list/{goPage}/{pageSize}")
    @RequiresRoles(RoleConstants.ADMIN)
    public Pager<List<User>> getOrgMemberListByAdmin(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryOrgMemberRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, userService.getOrgMemberList(request));
    }

    @PostMapping("/special/org/member/list/all")
    @RequiresRoles(RoleConstants.ADMIN)
    public List<User> getOrgMemberListByAdmin(@RequestBody QueryOrgMemberRequest request) {
        return userService.getOrgMemberList(request);
    }

    @GetMapping("/list")
    public List<User> getUserList() {
        return userService.getUserList();
    }

    @PostMapping("/update/current")
    @MsAuditLog(module = "personal_information_personal_settings", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#user.id)", content = "#msClass.getLogDetails(#user.id)", msClass = UserService.class)
    public UserDTO updateCurrentUser(@RequestBody User user) {
        String currentUserId = SessionUtils.getUserId();
        if (!StringUtils.equals(currentUserId, user.getId())) {
            MSException.throwException(Translator.get("not_authorized"));
        }
        userService.updateUser(user);
        UserDTO userDTO = userService.getUserDTO(user.getId());
        SessionUtils.putUser(SessionUser.fromUser(userDTO));
        return SessionUtils.getUser();
    }

    @PostMapping("/switch/source/org/{sourceId}")
    @RequiresRoles(value = {RoleConstants.ORG_ADMIN, RoleConstants.TEST_MANAGER, RoleConstants.TEST_VIEWER, RoleConstants.TEST_USER}, logical = Logical.OR)
    public UserDTO switchOrganization(@PathVariable(value = "sourceId") String sourceId) {
        userService.switchUserRole("organization", sourceId);
        return SessionUtils.getUser();
    }

    @PostMapping("/switch/source/ws/{sourceId}")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_VIEWER, RoleConstants.TEST_USER}, logical = Logical.OR)
    public UserDTO switchWorkspace(@PathVariable(value = "sourceId") String sourceId) {
        userService.switchUserRole("workspace", sourceId);
        return SessionUtils.getUser();
    }

    @PostMapping("/refresh/{sign}/{sourceId}")
    public UserDTO refreshSessionUser(@PathVariable String sign, @PathVariable String sourceId) {
        userService.refreshSessionUser(sign, sourceId);
        return SessionUtils.getUser();
    }

    @GetMapping("/info/{userId}")
    public UserDTO getUserInfo(@PathVariable(value = "userId") String userId) {
        if (!StringUtils.equals(userId, SessionUtils.getUserId())) {
            MSException.throwException(Translator.get("not_authorized"));
        }
        return userService.getUserInfo(userId);
    }

    /**
     * 获取工作空间成员用户
     */
    @PostMapping("/ws/member/list/{goPage}/{pageSize}")
    @RequiresRoles(value = {RoleConstants.ORG_ADMIN, RoleConstants.TEST_MANAGER,
            RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
    public Pager<List<User>> getMemberList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryMemberRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, userService.getMemberList(request));
    }

    /**
     * 获取工作空间成员用户 不分页
     */
    @PostMapping("/ws/member/list/all")
    @RequiresRoles(value = {RoleConstants.ORG_ADMIN, RoleConstants.TEST_MANAGER,
            RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
    public List<User> getMemberList(@RequestBody QueryMemberRequest request) {
        return userService.getMemberList(request);
    }

    /**
     * 添加工作空间成员
     */
    @PostMapping("/ws/member/add")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.ORG_ADMIN}, logical = Logical.OR)
    @MsAuditLog(module = "workspace_member", type = OperLogConstants.CREATE, title = "添加工作空间成员")
    public void addMember(@RequestBody AddMemberRequest request) {
        String wsId = request.getWorkspaceId();
        workspaceService.checkWorkspaceOwner(wsId);
        userService.addMember(request);
    }

    /**
     * 删除工作空间成员
     */
    @GetMapping("/ws/member/delete/{workspaceId}/{userId}")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.ORG_ADMIN}, logical = Logical.OR)
    @MsAuditLog(module = "workspace_member", type = OperLogConstants.DELETE, title = "删除工作空间成员")
    public void deleteMember(@PathVariable String workspaceId, @PathVariable String userId) {
        workspaceService.checkWorkspaceOwner(workspaceId);
        String currentUserId = SessionUtils.getUser().getId();
        if (StringUtils.equals(userId, currentUserId)) {
            MSException.throwException(Translator.get("cannot_remove_current"));
        }
        userService.deleteMember(workspaceId, userId);
    }

    /**
     * 添加组织成员
     */
    @PostMapping("/org/member/add")
    @RequiresRoles(RoleConstants.ORG_ADMIN)
    @MsAuditLog(module = "organization_member", type = OperLogConstants.CREATE, title = "添加组织成员")
    public void addOrganizationMember(@RequestBody AddOrgMemberRequest request) {
        organizationService.checkOrgOwner(request.getOrganizationId());
        userService.addOrganizationMember(request);
    }

    /**
     * 删除组织成员
     */
    @GetMapping("/org/member/delete/{organizationId}/{userId}")
    @RequiresRoles(RoleConstants.ORG_ADMIN)
    @MsAuditLog(module = "organization_member", type = OperLogConstants.DELETE, title = "删除组织成员")
    public void delOrganizationMember(@PathVariable String organizationId, @PathVariable String userId) {
        organizationService.checkOrgOwner(organizationId);
        String currentUserId = SessionUtils.getUser().getId();
        if (StringUtils.equals(userId, currentUserId)) {
            MSException.throwException(Translator.get("cannot_remove_current"));
        }
        userService.delOrganizationMember(organizationId, userId);
    }

    /**
     * 查询组织成员列表
     */
    @PostMapping("/org/member/list/{goPage}/{pageSize}")
    @RequiresRoles(value = {RoleConstants.ORG_ADMIN, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public Pager<List<User>> getOrgMemberList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryOrgMemberRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, userService.getOrgMemberList(request));
    }

    /**
     * 组织成员列表不分页
     */
    @PostMapping("/org/member/list/all")
    @RequiresRoles(value = {RoleConstants.ORG_ADMIN, RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public List<User> getOrgMemberList(@RequestBody QueryOrgMemberRequest request) {
        return userService.getOrgMemberList(request);
    }

    @GetMapping("/besideorg/list/{orgId}")
    public List<User> getBesideOrgMemberList(@PathVariable String orgId) {
        return userService.getBesideOrgMemberList(orgId);
    }

    /*
     * 修改当前用户密码
     * */
    @PostMapping("/update/password")
    @MsAuditLog(module = "system_user", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", content = "#msClass.getLogDetails(#request.id)", msClass = UserService.class)
    public int updateCurrentUserPassword(@RequestBody EditPassWordRequest request) {
        return userService.updateCurrentUserPassword(request);
    }

    /*管理员修改用户密码*/
    @PostMapping("/special/password")
    @MsAuditLog(module = "system_user", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", content = "#msClass.getLogDetails(#request.id)", msClass = UserService.class)
    public int updateUserPassword(@RequestBody EditPassWordRequest request) {
        return userService.updateUserPassword(request);
    }

    /**
     * 获取工作空间成员用户 不分页
     */
    @PostMapping("/ws/member/tester/list")
    @RequiresRoles(value = {RoleConstants.ORG_ADMIN, RoleConstants.TEST_MANAGER,
            RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
    public List<User> getTestManagerAndTestUserList(@RequestBody QueryMemberRequest request) {
        return userService.getTestManagerAndTestUserList(request);
    }

    @GetMapping("/search/{condition}")
    @RequiresRoles(value = {RoleConstants.ADMIN, RoleConstants.ORG_ADMIN, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public List<User> searchUser(@PathVariable String condition) {
        return userService.searchUser(condition);
    }

    @GetMapping("/export/template")
    @RequiresRoles(value = {RoleConstants.ADMIN, RoleConstants.ORG_ADMIN, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void testCaseTemplateExport(HttpServletResponse response) {
        userService.userTemplateExport(response);
    }

    @PostMapping("/import/{userId}")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public ExcelResponse testCaseImport(MultipartFile file, @PathVariable String userId) {
        return userService.userImport(file, userId);
    }

    @PostMapping("/special/batchProcessUserInfo")
    @RequiresRoles(value = {RoleConstants.ADMIN, RoleConstants.ORG_ADMIN, RoleConstants.TEST_MANAGER})
    @MsAuditLog(module = "system_user", type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.getLogDetails(#request)", content = "#msClass.getLogDetails(#request)", msClass = UserService.class)
    public String batchProcessUserInfo(@RequestBody UserBatchProcessRequest request) {
        String returnString = "success";
        userService.batchProcessUserInfo(request);
        return returnString;
    }

    @GetMapping("/getWorkspaceDataStruct/{organizationId}")
    public List<CascaderDTO> getWorkspaceDataStruct(@PathVariable String organizationId) {
        List<OrganizationMemberDTO> organizationList = organizationService.findIdAndNameByOrganizationId(organizationId);
        List<WorkspaceDTO> workspaceDTOList = workspaceService.findIdAndNameByOrganizationId(organizationId);
        if (!workspaceDTOList.isEmpty()) {
            Map<String, List<WorkspaceDTO>> orgIdWorkspaceMap = workspaceDTOList.stream().collect(Collectors.groupingBy(WorkspaceDTO::getOrganizationId));
            List<CascaderDTO> returnList = CascaderParse.parseWorkspaceDataStruct(organizationList, orgIdWorkspaceMap);
            return returnList;
        } else {
            return new ArrayList<>();
        }
    }

    @GetMapping("/getUserRoleDataStruct/{organizationId}")
    public List<CascaderDTO> getUserRoleDataStruct(@PathVariable String organizationId) {
        List<OrganizationMemberDTO> organizationList = organizationService.findIdAndNameByOrganizationId(organizationId);
        List<WorkspaceDTO> workspaceDTOList = workspaceService.findIdAndNameByOrganizationId(organizationId);
        if (!workspaceDTOList.isEmpty()) {
            Map<String, List<WorkspaceDTO>> orgIdWorkspaceMap = workspaceDTOList.stream().collect(Collectors.groupingBy(WorkspaceDTO::getOrganizationId));
            List<CascaderDTO> returnList = CascaderParse.parseUserRoleDataStruct(organizationList, orgIdWorkspaceMap, false);
            return returnList;
        } else {
            return new ArrayList<>();
        }
    }

    @GetMapping("/getWorkspaceUserRoleDataStruct/{organizationId}")
    public List<CascaderDTO> getWorkspaceUserRoleDataStruct(@PathVariable String organizationId) {
        List<OrganizationMemberDTO> organizationList = organizationService.findIdAndNameByOrganizationId(organizationId);
        List<WorkspaceDTO> workspaceDTOList = workspaceService.findIdAndNameByOrganizationId(organizationId);
        if (!workspaceDTOList.isEmpty()) {
            Map<String, List<WorkspaceDTO>> orgIdWorkspaceMap = workspaceDTOList.stream().collect(Collectors.groupingBy(WorkspaceDTO::getOrganizationId));
            List<CascaderDTO> returnList = CascaderParse.parseUserRoleDataStruct(organizationList, orgIdWorkspaceMap, true);
            return returnList;
        } else {
            return new ArrayList<>();
        }
    }


}
