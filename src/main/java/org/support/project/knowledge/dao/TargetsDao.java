package org.support.project.knowledge.dao;

import java.util.List;

import org.support.project.di.Container;
import org.support.project.ormapping.common.SQLManager;
import org.support.project.ormapping.dao.AbstractDao;
import org.support.project.web.bean.LabelValue;
import org.support.project.web.entity.GroupsEntity;
import org.support.project.web.entity.UsersEntity;

public class TargetsDao extends AbstractDao {
	/** SerialVersion */
	private static final long serialVersionUID = 1L;

	/**
	 * インスタンス取得
	 * @return インスタンス
	 */
	public static TargetsDao get() {
		return Container.getComp(TargetsDao.class);
	}
	
	/**
	 * キーワードで対象を検索（グループとアカウント）
	 * @param keyword
	 * @param offset
	 * @param limit
	 * @return
	 */
	public List<LabelValue> selectOnKeyword(String keyword, int offset, int limit) {
		String sql = SQLManager.getInstance().getSql("/org/support/project/knowledge/dao/sql/TargetsDao/selectOnKeyword.sql");
		return executeQueryList(sql, LabelValue.class, keyword, keyword, limit, offset);
	}

	
	
	/**
	 * ナレッジに指定されているアクセス可能なグループを取得
	 * @param knowledgeId
	 * @return
	 */
	public List<GroupsEntity> selectGroupsOnKnowledgeId(Long knowledgeId) {
		String sql = SQLManager.getInstance().getSql("/org/support/project/knowledge/dao/sql/TargetsDao/selectGroupsOnKnowledgeId.sql");
		return executeQueryList(sql, GroupsEntity.class, knowledgeId);
	}

	/**
	 * ナレッジに指定されているアクセス可能なユーザを取得
	 * @param knowledgeId
	 * @return
	 */
	public List<UsersEntity> selectUsersOnKnowledgeId(Long knowledgeId) {
		String sql = SQLManager.getInstance().getSql("/org/support/project/knowledge/dao/sql/TargetsDao/selectUsersOnKnowledgeId.sql");
		return executeQueryList(sql, UsersEntity.class, knowledgeId);
	}

	public List<GroupsEntity> selectEditorGroupsOnKnowledgeId(Long knowledgeId) {
		String sql = SQLManager.getInstance().getSql("/org/support/project/knowledge/dao/sql/TargetsDao/selectEditorGroupsOnKnowledgeId.sql");
		return executeQueryList(sql, GroupsEntity.class, knowledgeId);
	}

	public List<UsersEntity> selectEditorUsersOnKnowledgeId(Long knowledgeId) {
		String sql = SQLManager.getInstance().getSql("/org/support/project/knowledge/dao/sql/TargetsDao/selectEditorUsersOnKnowledgeId.sql");
		return executeQueryList(sql, UsersEntity.class, knowledgeId);
	}

	
}
