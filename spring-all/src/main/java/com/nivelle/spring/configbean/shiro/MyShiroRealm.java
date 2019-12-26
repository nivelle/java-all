package com.nivelle.spring.configbean.shiro;

import com.google.common.collect.Lists;
import com.nivelle.spring.springboot.entity.*;
import com.nivelle.spring.springboot.service.ShiroSysService;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;

public class MyShiroRealm extends AuthorizingRealm {

    private static final Logger logger = LoggerFactory.getLogger(MyShiroRealm.class);


    @Resource
    private ShiroSysService shiroSysService;

    /**
     * 授权
     *
     * @param principals
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        System.out.println("权限配置-->MyShiroRealm.doGetAuthorizationInfo()");
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        UserInfoEntity userInfo = (UserInfoEntity) principals.getPrimaryPrincipal();
        logger.info("roles:{}", userInfo.getRoleList());
        for (SysRoleEntity role : userInfo.getRoleList()) {
            authorizationInfo.addRole(role.getRole());
            logger.info("permissions:{}", role.getPermissions());
            for (SysPermissionEntity p : role.getPermissions()) {
                authorizationInfo.addStringPermission(p.getPermission());
            }
        }
        return authorizationInfo;
    }

    /**
     * 认证
     *
     * @param token
     * @return
     * @throws AuthenticationException
     */
    /*主要是用来进行身份认证的，也就是说验证用户输入的账号和密码是否正确。*/
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
            throws AuthenticationException {
        logger.info("身份校验,token:{}", token);
        //获取用户的输入的账号.
        String name = (String) token.getPrincipal();
        logger.info("credentials={}", token.getCredentials());
        //通过username从数据库中查找 User对象，如果找到，没找到.
        //实际项目中，这里可以根据实际情况做缓存，如果不做，Shiro自己也是有时间间隔机制，2分钟内不会重复执行该方法
        UserInfoEntity userInfo = shiroSysService.findByUsername(name);

        long uid = userInfo.getUid();

        List<SystemUserRoleEntity> systemUserRoleEntities = shiroSysService.getSysUserRoleListByUid(uid);
        List<SysRoleEntity> roleEntities = Lists.newArrayList();
        List<SysPermissionEntity> sysPermissionEntities = Lists.newArrayList();
        for (SystemUserRoleEntity systemUserRoleEntity : systemUserRoleEntities) {
            SysRoleEntity sysRoleEntity = shiroSysService.getSysRoleEntityById(systemUserRoleEntity.getRoleId());
            List<SysRolePermissionEntity> sysRolePermissionEntities = shiroSysService.getSysRolePermissionEntityByRoleId(sysRoleEntity.getId());

            for (SysRolePermissionEntity sysRolePermissionEntity : sysRolePermissionEntities) {
                SysPermissionEntity sysPermissionEntity = shiroSysService.getSysPermissionEntityById(
                        sysRolePermissionEntity.getPermissionId());
                sysPermissionEntities.add(sysPermissionEntity);
            }
            sysRoleEntity.setPermissions(sysPermissionEntities);
            roleEntities.add(sysRoleEntity);
        }
        userInfo.setRoleList(roleEntities);
        logger.info("查询到的用户信息,userInfo={}", userInfo);
        if (userInfo == null) {
            return null;
        }
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                userInfo, //用户名
                userInfo.getPassword(), //密码
                ByteSource.Util.bytes(userInfo.getCredentialsSalt()),//salt=username+salt
                getName()  //realm name
        );
        return authenticationInfo;
    }

}