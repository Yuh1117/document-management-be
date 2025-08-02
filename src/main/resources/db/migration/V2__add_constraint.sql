ALTER TABLE ONLY public.user_group_members
    ADD CONSTRAINT user_group_unique UNIQUE (user_id, group_id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT email_unique UNIQUE (email);

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT name_unique UNIQUE (name);

CREATE UNIQUE INDEX unique_folder_name_per_parent
    ON public.folders(parent_id, name)
    WHERE parent_id IS NOT NULL;

CREATE UNIQUE INDEX unique_root_folder_name
    ON public.folders(name)
    WHERE parent_id IS NULL;
--
ALTER TABLE ONLY public.access_logs
    ADD CONSTRAINT access_logs_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.document_permissions
    ADD CONSTRAINT document_permissions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.document_tag_assignments
    ADD CONSTRAINT document_tag_assignments_pkey PRIMARY KEY (document_id, document_tag_id);

ALTER TABLE ONLY public.document_tags
    ADD CONSTRAINT document_tags_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.document_versions
    ADD CONSTRAINT document_versions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT documents_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.folder_permissions
    ADD CONSTRAINT folder_permissions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.folders
    ADD CONSTRAINT folders_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.otp_codes
    ADD CONSTRAINT otp_codes_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.role_permission
    ADD CONSTRAINT role_permission_pkey PRIMARY KEY (role_id, permission_id);

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.system_settings
    ADD CONSTRAINT system_settings_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.user_group_members
    ADD CONSTRAINT user_group_members_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.user_groups
    ADD CONSTRAINT user_groups_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);
--
ALTER TABLE ONLY public.access_logs
    ADD CONSTRAINT fk_access_logs_document FOREIGN KEY (document_id) REFERENCES public.documents(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ONLY public.access_logs
    ADD CONSTRAINT fk_access_logs_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE ON UPDATE CASCADE;
--
ALTER TABLE ONLY public.document_permissions
    ADD CONSTRAINT fk_document_permissions_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ONLY public.document_permissions
    ADD CONSTRAINT fk_document_permissions_document FOREIGN KEY (document_id) REFERENCES public.documents(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ONLY public.document_permissions
    ADD CONSTRAINT fk_document_permissions_group FOREIGN KEY (group_id) REFERENCES public.user_groups(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ONLY public.document_permissions
    ADD CONSTRAINT fk_document_permissions_updated_by FOREIGN KEY (updated_by) REFERENCES public.users(id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE ONLY public.document_permissions
    ADD CONSTRAINT fk_document_permissions_created_by FOREIGN KEY (created_by) REFERENCES public.users(id) ON DELETE CASCADE ON UPDATE CASCADE;
--
ALTER TABLE ONLY public.document_tag_assignments
    ADD CONSTRAINT fk_document_tag_assignments_document FOREIGN KEY (document_id) REFERENCES public.documents(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ONLY public.document_tag_assignments
    ADD CONSTRAINT fk_document_tag_assignments_tag FOREIGN KEY (document_tag_id) REFERENCES public.document_tags(id) ON DELETE CASCADE ON UPDATE CASCADE;
--
ALTER TABLE ONLY public.document_tags
    ADD CONSTRAINT fk_document_tags_updated_by FOREIGN KEY (updated_by) REFERENCES public.users(id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE ONLY public.document_tags
    ADD CONSTRAINT fk_document_tags_created_by FOREIGN KEY (created_by) REFERENCES public.users(id) ON DELETE CASCADE ON UPDATE CASCADE;
--
ALTER TABLE ONLY public.document_versions
    ADD CONSTRAINT fk_document_versions_document FOREIGN KEY (document_id) REFERENCES public.documents(id) ON DELETE CASCADE ON UPDATE CASCADE;
--
ALTER TABLE ONLY public.documents
    ADD CONSTRAINT fk_documents_folder FOREIGN KEY (folder_id) REFERENCES public.folders(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT fk_documents_created_by FOREIGN KEY (created_by) REFERENCES public.users(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT fk_documents_updated_by FOREIGN KEY (updated_by) REFERENCES public.users(id) ON DELETE SET NULL ON UPDATE CASCADE;
--
ALTER TABLE ONLY public.folder_permissions
    ADD CONSTRAINT fk_folder_permissions_created_by FOREIGN KEY (created_by) REFERENCES public.users(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ONLY public.folder_permissions
    ADD CONSTRAINT fk_folder_permissions_group FOREIGN KEY (group_id) REFERENCES public.user_groups(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ONLY public.folder_permissions
    ADD CONSTRAINT fk_folder_permissions_folder FOREIGN KEY (folder_id) REFERENCES public.folders(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ONLY public.folder_permissions
    ADD CONSTRAINT fk_folder_permissions_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ONLY public.folder_permissions
    ADD CONSTRAINT fk_folder_permissions_updated_by FOREIGN KEY (updated_by) REFERENCES public.users(id) ON DELETE SET NULL ON UPDATE CASCADE;
--
ALTER TABLE ONLY public.folders
    ADD CONSTRAINT fk_folders_created_by FOREIGN KEY (created_by) REFERENCES public.users(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ONLY public.folders
    ADD CONSTRAINT fk_folders_parent FOREIGN KEY (parent_id) REFERENCES public.folders(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ONLY public.folders
    ADD CONSTRAINT fk_folders_updated_by FOREIGN KEY (updated_by) REFERENCES public.users(id) ON DELETE SET NULL ON UPDATE CASCADE;
--
ALTER TABLE ONLY public.otp_codes
    ADD CONSTRAINT fk_otp_codes_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE ON UPDATE CASCADE;
--
ALTER TABLE ONLY public.role_permission
    ADD CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES public.permissions(id) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE ONLY public.role_permission
    ADD CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES public.roles(id) ON DELETE RESTRICT ON UPDATE CASCADE;
--
ALTER TABLE ONLY public.system_settings
    ADD CONSTRAINT fk_system_settings_updated_by FOREIGN KEY (updated_by) REFERENCES public.users(id) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE ONLY public.system_settings
    ADD CONSTRAINT fk_system_settings_created_by FOREIGN KEY (created_by) REFERENCES public.users(id) ON DELETE RESTRICT ON UPDATE CASCADE;
--
ALTER TABLE ONLY public.user_group_members
    ADD CONSTRAINT fk_user_group_members_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ONLY public.user_group_members
    ADD CONSTRAINT fk_user_group_members_group FOREIGN KEY (group_id) REFERENCES public.user_groups(id) ON DELETE CASCADE ON UPDATE CASCADE;
--
ALTER TABLE ONLY public.user_groups
    ADD CONSTRAINT fk_user_groups_updated_by FOREIGN KEY (updated_by) REFERENCES public.users(id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE ONLY public.user_groups
    ADD CONSTRAINT fk_user_groups_created_by FOREIGN KEY (created_by) REFERENCES public.users(id) ON DELETE CASCADE ON UPDATE CASCADE;
--
ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES public.roles(id) ON DELETE RESTRICT ON UPDATE CASCADE;