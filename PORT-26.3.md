# Minecraft 26.3 compatibility

Added `versions/26.3/gradle.properties` targeting Minecraft 26.3, Loader
0.19.5, and Fabric API 0.161.0+26.3. The shared command implementation,
dependency relocation, Java 26 build toolchain, and Java 25 bytecode target are
preserved.

Minecraft 26.3 adds `System chat: ` to `MinecraftServer.sendSystemMessage`
console output. Existing MCDR 2.15.7 plugins match the previous output format
when querying the seed, entity data, and player events. The 26.3 artifact
includes a server-only Mixin that changes only the format literal
`System chat: {}` back to `{}`. All other logging and message content is
unchanged. Older build targets do not declare or compile this Mixin.

The runtime artifact was verified on macOS ARM64 with OpenJDK 25.0.4.1,
Minecraft 26.3, Fabric Loader 0.19.5, and Fabric API 0.161.0+26.3. An isolated
MCDR instance verified HTTP command registration, entity-data responses, and
the absence of the seed and player-data query timeouts seen before restoring
the console format. No interactive client completion session was tested.
