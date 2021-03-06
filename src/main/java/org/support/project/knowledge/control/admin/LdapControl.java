package org.support.project.knowledge.control.admin;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import net.arnx.jsonic.JSONException;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.support.project.common.bean.ValidateError;
import org.support.project.common.config.INT_FLAG;
import org.support.project.common.util.PasswordUtil;
import org.support.project.common.util.StringUtils;
import org.support.project.di.DI;
import org.support.project.di.Instance;
import org.support.project.knowledge.config.AppConfig;
import org.support.project.knowledge.control.Control;
import org.support.project.web.annotation.Auth;
import org.support.project.web.bean.LdapInfo;
import org.support.project.web.boundary.Boundary;
import org.support.project.web.control.service.Get;
import org.support.project.web.control.service.Post;
import org.support.project.web.dao.LdapConfigsDao;
import org.support.project.web.entity.LdapConfigsEntity;
import org.support.project.web.exception.InvalidParamException;
import org.support.project.web.logic.LdapLogic;

@DI(instance=Instance.Prototype)
public class LdapControl extends Control {
	
	private static final String NO_CHANGE_PASSWORD = "NO_CHANGE_PASSWORD-fXLSJ_V-ZJ2E-X6c2_iGCpkE"; //パスワードを更新しなかったことを表すパスワード
	
	/**
	 * 設定画面を表示
	 * @return
	 */
	@Get
	@Auth(roles="admin")
	public Boundary config() {
		LdapConfigsDao dao = LdapConfigsDao.get();
		LdapConfigsEntity entity = dao.selectOnKey(AppConfig.get().getSystemName());
		if (entity == null) {
			entity = new LdapConfigsEntity();
		} else {
			entity.setBindPassword(NO_CHANGE_PASSWORD);
			entity.setSalt("");
		}
		entity.setSystemName(AppConfig.get().getSystemName());
		setAttributeOnProperty(entity);
		
		if (entity.getUseSsl() != null && entity.getUseSsl().intValue() == INT_FLAG.ON.getValue()) {
			setAttribute("security", "usessl");
		} else if (entity.getUseTls() != null && entity.getUseTls().intValue() == INT_FLAG.ON.getValue()) {
			setAttribute("security", "usetls");
		} else {
			setAttribute("security", "plain");
		}
		return forward("config.jsp");
	}
	
	/**
	 * リクエストの情報からLdapの設定情報を抽出（共通処理）
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws InvalidParamException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	private LdapConfigsEntity loadLdapConfig() throws InstantiationException, IllegalAccessException, IOException, InvalidParamException,
			NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		LdapConfigsDao dao = LdapConfigsDao.get();
		LdapConfigsEntity entity = super.getParamOnProperty(LdapConfigsEntity.class);
		String security = getParam("security");
		if (!StringUtils.isEmpty(security)) {
			if (security.toLowerCase().equals("usessl")) {
				entity.setUseSsl(INT_FLAG.ON.getValue());
			} else if (security.toLowerCase().equals("usetls")) {
				entity.setUseTls(INT_FLAG.ON.getValue());
			}
		}
		String password = entity.getBindPassword();
		if (password.equals(NO_CHANGE_PASSWORD)) {
			LdapConfigsEntity saved = dao.selectOnKey(AppConfig.get().getSystemName());
			if (saved != null) {
				String encPass = saved.getBindPassword();
				String salt = saved.getSalt();
				password = PasswordUtil.decrypt(encPass, salt);
				entity.setBindPassword(password);
			}
		}
		return entity;
	}	
	
	/**
	 * Ldap認証の設定のテスト
	 * @return
	 * @throws InvalidParamException 
	 * @throws IOException 
	 * @throws JSONException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws LdapException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	@Post
	@Auth(roles="admin")
	public Boundary check() throws InstantiationException, IllegalAccessException, JSONException, IOException, InvalidParamException, LdapException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		List<ValidateError> errors = LdapConfigsEntity.get().validate(getParams());
		if (errors != null && !errors.isEmpty()) {
			super.setResult("", errors);
			return forward("config.jsp");
		}
		LdapConfigsEntity entity = loadLdapConfig();
		
		LdapLogic ldapLogic = LdapLogic.get();
		LdapInfo result = ldapLogic.auth(entity, entity.getBindDn(), entity.getBindPassword());
		if (result == null) {
			addMsgWarn("knowledge.ldap.msg.connect.error");
		} else {
			addMsgSuccess("knowledge.ldap.msg.connect.success"
					, result.getId()
					, result.getName() 
					, result.getMail() 
					, String.valueOf(result.isAdmin()));
		}
		return forward("config.jsp");
	}



	
	/**
	 * Ldap認証の設定のテスト
	 * @return
	 * @throws InvalidParamException 
	 * @throws IOException 
	 * @throws JSONException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws LdapException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	@Post
	@Auth(roles="admin")
	public Boundary save() throws InstantiationException, IllegalAccessException, JSONException, IOException, InvalidParamException, LdapException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		List<ValidateError> errors = LdapConfigsEntity.get().validate(getParams());
		if (errors != null && !errors.isEmpty()) {
			super.setResult("", errors);
			return forward("config.jsp");
		}
		LdapConfigsEntity entity = loadLdapConfig();
		LdapLogic ldapLogic = LdapLogic.get();
		LdapInfo result = ldapLogic.auth(entity, entity.getBindDn(), entity.getBindPassword());
		if (result == null) {
			addMsgWarn("knowledge.ldap.msg.save.error");
		} else {
			//Ldap設定を保存
			LdapConfigsDao dao = LdapConfigsDao.get();
			entity.setSystemName(AppConfig.get().getSystemName());
			String salt = PasswordUtil.getSalt();
			String passHash = PasswordUtil.encrypt(entity.getBindPassword(), salt);
			entity.setBindPassword(passHash);
			entity.setSalt(salt);
			dao.save(entity);
			
			entity.setBindPassword(NO_CHANGE_PASSWORD);
			setAttributeOnProperty(entity);
			addMsgSuccess("knowledge.ldap.msg.save.success");
		}
		return forward("config.jsp");
	}
	
	/**
	 * Ldap設定の削除
	 * @return
	 */
	@Post
	@Auth(roles="admin")
	public Boundary delete() {
		LdapConfigsDao dao = LdapConfigsDao.get();
		LdapConfigsEntity entity = dao.selectOnKey(AppConfig.get().getSystemName());
		if (entity != null) {
			dao.physicalDelete(AppConfig.get().getSystemName());
		}
		entity = new LdapConfigsEntity();
		entity.setSystemName(AppConfig.get().getSystemName());
		setAttributeOnProperty(entity);
		
		addMsgInfo("message.success.delete.target", getResource("knowledge.ldap.title"));
		
		return config();
	}
	
}
