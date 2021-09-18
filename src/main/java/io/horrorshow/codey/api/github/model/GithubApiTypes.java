package io.horrorshow.codey.api.github.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;


public class GithubApiTypes {

    public static class Ping {

        @JsonProperty public String zen;
        @JsonProperty public String hook_id;
        @JsonProperty public Map<String, Object> hook;
        @JsonProperty public Repository repository;
        @JsonProperty public UserInfo sender;
    }


    public static class CheckRunPayload {
        @JsonProperty public String action;
        @JsonProperty public CheckRun check_run;
        @JsonProperty public Repository repository;
        @JsonProperty public UserInfo sender;
    }


    public static class CheckRun {
        @JsonProperty public Long id;
        @JsonProperty public String node_id;
        @JsonProperty public String head_sha;
        @JsonProperty public String external_id;
        @JsonProperty public String url;
        @JsonProperty public String html_url;
        @JsonProperty public String details_url;
        @JsonProperty public String status;
        @JsonProperty public String conclusion;
        @JsonProperty public String started_at;
        @JsonProperty public String completed_at;
        @JsonProperty public Output output;
        @JsonProperty public String name;
        @JsonProperty public CheckSuite check_suite;
        @JsonProperty public App app;
        @JsonProperty public List<PullRequest> pull_requests;
        @JsonProperty public Deployment deployment;
    }


    public static class Deployment {
        @JsonProperty public String url;
        @JsonProperty public Long id;
        @JsonProperty public String node_id;
        @JsonProperty public String task;
        @JsonProperty public String original_environment;
        @JsonProperty public String environment;
        @JsonProperty public String description;
        @JsonProperty public String created_at;
        @JsonProperty public String updated_at;
        @JsonProperty public String statuses_url;
        @JsonProperty public String repository_url;
    }


    public static class CheckSuite {
        @JsonProperty public Long id;
        @JsonProperty public String node_id;
        @JsonProperty public String head_branch;
        @JsonProperty public String head_sha;
        @JsonProperty public String status;
        @JsonProperty public String conclusion;
        @JsonProperty public String url;
        @JsonProperty public String before;
        @JsonProperty public String after;
        @JsonProperty public List<PullRequest> pull_requests;
        @JsonProperty public App app;
        @JsonProperty public String created_at;
        @JsonProperty public String updated_at;
    }


    static class App {
        @JsonProperty public Long id;
        @JsonProperty public String node_id;
        @JsonProperty public UserInfo owner;
        @JsonProperty public String name;
        @JsonProperty public String description;
        @JsonProperty public String external_url;
        @JsonProperty public String html_url;
        @JsonProperty public String created_at;
        @JsonProperty public String updated_at;
        @JsonProperty public Permissions permissions;
        @JsonProperty public List<Object> events;
    }


    static class Permissions {
        @JsonProperty public String administration;
        @JsonProperty public String checks;
        @JsonProperty public String contents;
        @JsonProperty public String deployments;
        @JsonProperty public String issues;
        @JsonProperty public String members;
        @JsonProperty public String metadata;
        @JsonProperty public String organization_administration;
        @JsonProperty public String organization_hooks;
        @JsonProperty public String organization_plan;
        @JsonProperty public String organization_projects;
        @JsonProperty public String organization_user_blocking;
        @JsonProperty public String pages;
        @JsonProperty public String pull_requests;
        @JsonProperty public String repository_hooks;
        @JsonProperty public String repository_projects;
        @JsonProperty public String statuses;
        @JsonProperty public String team_discussions;
        @JsonProperty public String vulnerability_alerts;
    }


    static class PullRequest {
        @JsonProperty public String url;
        @JsonProperty public Long id;
        @JsonProperty public Integer number;
        @JsonProperty public BranchRef head;
        @JsonProperty public BranchRef base;
    }


    static class BranchRef {
        @JsonProperty public String ref;
        @JsonProperty public String sha;
        @JsonProperty public RepoInfo repo;
    }

    static class RepoInfo {
        @JsonProperty public Long id;
        @JsonProperty public String url;
        @JsonProperty public String name;
    }


    public static class Output {
        @JsonProperty public String title;
        @JsonProperty public String summary;
        @JsonProperty public String text;
        @JsonProperty public String annotations_count;
        @JsonProperty public String annotations_url;
    }


    public static class WorkflowRunPayload {
        @JsonProperty public String action;
        @JsonProperty public Organization organization;
        @JsonProperty public Repository repository;
        @JsonProperty public UserInfo sender;
    }


    public static class Organization {
        @JsonProperty public String avatar_url;
        @JsonProperty public String description;
        @JsonProperty public String events_url;
        @JsonProperty public String hooks_url;
        @JsonProperty public Long id;
        @JsonProperty public String issues_url;
        @JsonProperty public String login;
        @JsonProperty public String members_url;
        @JsonProperty public String node_id;
        @JsonProperty public String public_members_url;
        @JsonProperty public String repos_url;
        @JsonProperty public String url;
    }


    public static class WorkflowPayload {

        @JsonProperty public String action;
        @JsonProperty public WorkflowJob workflow_job;
        @JsonProperty public Repository repository;
        @JsonProperty public UserInfo sender;
    }


    public static class WorkflowJob {

        @JsonProperty public Long id;
        @JsonProperty public Long run_id;
        @JsonProperty public String run_url;
        @JsonProperty public String node_id;
        @JsonProperty public String head_sha;
        @JsonProperty public String url;
        @JsonProperty public String html_url;
        @JsonProperty public String status;
        @JsonProperty public Object conclusion;
        @JsonProperty public String started_at;
        @JsonProperty public String completed_at;
        @JsonProperty public String name;
        @JsonProperty public List<Object> steps;
        @JsonProperty public String check_run_url;
        @JsonProperty public List<String> labels;
        @JsonProperty public String runner_id;
        @JsonProperty public String runner_name;
        @JsonProperty public String runner_group_id;
        @JsonProperty public String runner_group_name;
    }


    public static class PageBuild {

        @JsonProperty public String id;
        @JsonProperty public Build build;
        @JsonProperty public Repository repository;
        @JsonProperty public UserInfo sender;
    }


    public static class Build {

        @JsonProperty public String url;
        @JsonProperty public String status;
        @JsonProperty public Map<String, String> error;
        @JsonProperty public UserInfo pusher;
        @JsonProperty public String commit;
        @JsonProperty public Long duration;
        @JsonProperty public String created_at;
        @JsonProperty public String updated_at;
    }


    public static class Commit {

        @JsonProperty public String id;
        @JsonProperty public String tree_id;
        @JsonProperty public boolean distinct;
        @JsonProperty public String message;
        @JsonProperty public String timestamp;
        @JsonProperty public String url;
        @JsonProperty public User author;
        @JsonProperty public User committer;
        @JsonProperty public List<String> added;
        @JsonProperty public List<String> modified;
        @JsonProperty public List<String> removed;

    }

    public static class User {

        @JsonProperty public String name;
        @JsonProperty public String email;
        @JsonProperty public String username;
    }


    public static class Push {

        @JsonProperty public String ref;
        @JsonProperty public String before;
        @JsonProperty public String after;
        @JsonProperty public Repository repository;
        @JsonProperty public User pusher;
        @JsonProperty public UserInfo sender;
        @JsonProperty public boolean created;
        @JsonProperty public boolean deleted;
        @JsonProperty public boolean forced;
        @JsonProperty public Object base_ref;
        @JsonProperty public String compare;
        @JsonProperty public List<Commit> commits;
        @JsonProperty public Commit head_commit;
    }

    public static class UserInfo {

        @JsonProperty public String name;
        @JsonProperty public String email;
        @JsonProperty public String login;
        @JsonProperty public Long id;
        @JsonProperty public String node_id;
        @JsonProperty public String avatar_url;
        @JsonProperty public String gravatar_id;
        @JsonProperty public String url;
        @JsonProperty public String html_url;
        @JsonProperty public String followers_url;
        @JsonProperty public String following_url;
        @JsonProperty public String gists_url;
        @JsonProperty public String starred_url;
        @JsonProperty public String subscriptions_url;
        @JsonProperty public String organizations_url;
        @JsonProperty public String repos_url;
        @JsonProperty public String events_url;
        @JsonProperty public String received_events_url;
        @JsonProperty public String type;
        @JsonProperty public boolean site_admin;
    }

    public static class Repository {

        @JsonProperty public Long id;
        @JsonProperty public String node_id;
        @JsonProperty public String name;
        @JsonProperty public String full_name;
        @JsonProperty("private") public boolean is_private;
        @JsonProperty public UserInfo owner;
        @JsonProperty public String html_url;
        @JsonProperty public String description;
        @JsonProperty public boolean fork;
        @JsonProperty public String url;
        @JsonProperty public String forks_url;
        @JsonProperty public String keys_url;
        @JsonProperty public String collaborators_url;
        @JsonProperty public String teams_url;
        @JsonProperty public String hooks_url;
        @JsonProperty public String issue_events_url;
        @JsonProperty public String events_url;
        @JsonProperty public String assignees_url;
        @JsonProperty public String branches_url;
        @JsonProperty public String tags_url;
        @JsonProperty public String blobs_url;
        @JsonProperty public String git_tags_url;
        @JsonProperty public String git_refs_url;
        @JsonProperty public String trees_url;
        @JsonProperty public String statuses_url;
        @JsonProperty public String languages_url;
        @JsonProperty public String stargazers_url;
        @JsonProperty public String contributors_url;
        @JsonProperty public String subscribers_url;
        @JsonProperty public String subscription_url;
        @JsonProperty public String commits_url;
        @JsonProperty public String git_commits_url;
        @JsonProperty public String comments_url;
        @JsonProperty public String issue_comment_url;
        @JsonProperty public String contents_url;
        @JsonProperty public String compare_url;
        @JsonProperty public String merges_url;
        @JsonProperty public String archive_url;
        @JsonProperty public String downloads_url;
        @JsonProperty public String issues_url;
        @JsonProperty public String pulls_url;
        @JsonProperty public String milestones_url;
        @JsonProperty public String notifications_url;
        @JsonProperty public String labels_url;
        @JsonProperty public String releases_url;
        @JsonProperty public String deployments_url;
        @JsonProperty public String created_at;
        @JsonProperty public String updated_at;
        @JsonProperty public String pushed_at;
        @JsonProperty public String git_url;
        @JsonProperty public String ssh_url;
        @JsonProperty public String clone_url;
        @JsonProperty public String svn_url;
        @JsonProperty public String homepage;
        @JsonProperty public Integer size;
        @JsonProperty public Integer stargazers_count;
        @JsonProperty public Integer watchers_count;
        @JsonProperty public String language;
        @JsonProperty public boolean has_issues;
        @JsonProperty public boolean has_projects;
        @JsonProperty public boolean has_downloads;
        @JsonProperty public boolean has_wiki;
        @JsonProperty public boolean has_pages;
        @JsonProperty public Integer forks_count;
        @JsonProperty public String mirror_url;
        @JsonProperty public boolean archived;
        @JsonProperty public boolean disabled;
        @JsonProperty public Integer open_issues_count;
        @JsonProperty public Map<String, String> license;
        @JsonProperty public boolean allow_forking;
        @JsonProperty public Integer forks;
        @JsonProperty public Integer open_issues;
        @JsonProperty public Integer watchers;
        @JsonProperty public String default_branch;
        @JsonProperty public Integer stargazers;
        @JsonProperty public String master_branch;
    }
}
