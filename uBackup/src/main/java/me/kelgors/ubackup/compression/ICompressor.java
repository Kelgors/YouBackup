package me.kelgors.ubackup.compression;

import me.kelgors.ubackup.WorldConfiguration;
import org.bukkit.World;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public interface ICompressor {

    void prepare(WorldConfiguration configuration);
    CompletableFuture<File> compress(World world);
}
