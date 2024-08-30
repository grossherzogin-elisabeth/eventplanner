package org.eventplanner.users.adapter.filesystem;

import org.eventplanner.users.adapter.UserRepository;
import org.eventplanner.users.entities.EncryptedUserDetails;
import org.eventplanner.users.values.UserKey;
import org.eventplanner.utils.FileSystemJsonRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.List;
import java.util.Optional;

// TODO encrypt user data
@Repository
public class UserFileSystemRepository implements UserRepository {

    private final FileSystemJsonRepository<EncryptedUserDetails> fs;

    public UserFileSystemRepository(
        @Value("${custom.data-directory}") String dataDirectory
    ) {
        var directory = new File(dataDirectory + "/users");
        this.fs = new FileSystemJsonRepository<>(EncryptedUserDetails.class, directory);
    }

    @Override
    public @NonNull List<EncryptedUserDetails> findAll() {
        return fs.findAll();
    }

    @Override
    public @NonNull Optional<EncryptedUserDetails> findByKey(@NonNull UserKey key) {
        return fs.findByKey(key.value());
    }

    @Override
    public @NonNull EncryptedUserDetails create(@NonNull EncryptedUserDetails user) {
        fs.save(user.getKey().value(), user);
        return user;
    }

    @Override
    public @NonNull EncryptedUserDetails update(@NonNull EncryptedUserDetails user) {
        fs.save(user.getKey().value(), user);
        return user;
    }

    @Override
    public void deleteAll() {
        fs.deleteAll();
    }
}