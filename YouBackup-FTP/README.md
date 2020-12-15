## YouBackup-FTP

YouBackup extension to send your files over ftp. **SFTP** not supported.

:exclamation: This means that plugin will send **without encryption** your username/password/files as any FTP clients. 
I do not recommend to use FTP to store backup data, especially for large servers.

### config.yml (in plugins/YouBackup)

```yaml
backups:
  your_profile:
    # ...
    destination:
      # Set it to ftp
      type: ftp
      # ftp connection information
      host: "192.168.0.1"
      port: 21
      # ftp login credentials
      username: admin
      password: password
      # remote directory
      # ! THIS DIRECTORY NEEDS TO BE DEDICATED TO THIS BACKUP PROFILE !
      # ! OTHERWISE ROTATION DONT WORK !
      path: backups/your_profile
```
