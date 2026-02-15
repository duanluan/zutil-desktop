package top.zhjh.util

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Guards desktop app startup so only one process instance can run at a time.
 */
class SingleInstanceHandle private constructor(
  private val lockFile: Path,
  private val pidFile: Path,
  private val channel: FileChannel,
  private val lock: FileLock
) : AutoCloseable {
  private val closed = AtomicBoolean(false)
  private val shutdownHook = Thread({ close() }, "zutil-single-instance-cleanup").apply {
    isDaemon = true
  }

  init {
    Runtime.getRuntime().addShutdownHook(shutdownHook)
  }

  override fun close() {
    if (!closed.compareAndSet(false, true)) return

    runCatching { lock.release() }
    runCatching { channel.close() }
    runCatching { Files.deleteIfExists(pidFile) }
    runCatching { Files.deleteIfExists(lockFile) }
    runCatching { Runtime.getRuntime().removeShutdownHook(shutdownHook) }
  }

  companion object {
    fun tryAcquire(appId: String): SingleInstanceHandle? {
      val runtimeDir = buildRuntimeDir(appId)
      Files.createDirectories(runtimeDir)

      val lockFile = runtimeDir.resolve("$appId.lock")
      val pidFile = runtimeDir.resolve("$appId.pid")
      val channel = FileChannel.open(
        lockFile,
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE
      )

      val lock = try {
        channel.tryLock()
      } catch (_: OverlappingFileLockException) {
        null
      } catch (_: Exception) {
        null
      }

      if (lock == null) {
        runCatching { channel.close() }
        return null
      }

      writeRuntimeMetadata(channel, pidFile, appId)
      return SingleInstanceHandle(lockFile, pidFile, channel, lock)
    }

    private fun buildRuntimeDir(appId: String): Path {
      val localAppData = System.getenv("LOCALAPPDATA")
      return if (!localAppData.isNullOrBlank()) {
        Path.of(localAppData, appId, "runtime")
      } else {
        Path.of(System.getProperty("user.home"), ".$appId", "runtime")
      }
    }

    private fun writeRuntimeMetadata(channel: FileChannel, pidFile: Path, appId: String) {
      val pid = ProcessHandle.current().pid()
      val now = Instant.now()
      val metadata = buildString {
        appendLine("appId=$appId")
        appendLine("pid=$pid")
        appendLine("startedAt=$now")
      }

      val bytes = metadata.toByteArray(StandardCharsets.UTF_8)
      channel.truncate(0)
      channel.position(0)
      channel.write(ByteBuffer.wrap(bytes))
      channel.force(true)

      Files.writeString(
        pidFile,
        metadata,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE
      )
    }
  }
}

object SingleInstanceGuard {
  fun tryAcquire(appId: String = "zutil-desktop"): SingleInstanceHandle? {
    return SingleInstanceHandle.tryAcquire(appId)
  }
}
