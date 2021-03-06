package nz.co.trineo.repo;

import java.util.List;

import org.eclipse.egit.github.core.RepositoryCommit;

import nz.co.trineo.common.Service;
import nz.co.trineo.model.Branch;
import nz.co.trineo.model.GitDiff;
import nz.co.trineo.model.Repository;
import nz.co.trineo.model.Tag;
import nz.co.trineo.model.TreeNode;

public interface RepoService extends Service {
	void checkout(long id) throws RepoServiceException;

	Repository createRepo(String url, int accId) throws RepoServiceException;

	List<GitDiff> diffBranches(long id, long compareId) throws RepoServiceException;

	TreeNode getDiffTree(List<GitDiff> list) throws RepoServiceException;

	Branch createBranch(int id, String branchName) throws RepoServiceException;

	void deleteRepo(int id) throws RepoServiceException;

	List<Branch> getBranches(int id) throws RepoServiceException;

	List<Branch> updateBranches(int id) throws RepoServiceException;

	List<Tag> updateTags(int id) throws RepoServiceException;

	RepositoryCommit getCommit(int id, String sha1) throws RepoServiceException;

	List<String> getCommits(int id) throws RepoServiceException;

	Repository getRepo(int id) throws RepoServiceException;

	List<Repository> getRepos() throws RepoServiceException;

	List<Tag> getTags(int id) throws RepoServiceException;

	Repository updateRepo(Repository repo) throws RepoServiceException;

	boolean isRepo(int id) throws RepoServiceException;

	void clone(final int id) throws RepoServiceException;

	void pull(final int id) throws RepoServiceException;
}
