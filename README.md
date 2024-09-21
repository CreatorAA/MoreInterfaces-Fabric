# MoreInterfaces
[跳转到中文版本](#README_cn.md)
MoreInterfaces is a mod that provides third-party network interfaces for Minecraft. To make it easier to understand, you can think of it as a super simple RPC plugin.

## Introduction

MoreInterfaces serves third-party developers, including regular players, and aims to provide nearly all versions of Minecraft's network API interfaces (from `1.14` and above) for third-party software control. With built-in multiple APIs, you can achieve dynamic service updates externally!

Currently supported: Fabric, Forge, NeoForge  
Future support planned: Bukkit, Quilt

## Note

We do not recommend any third-party developers to create auxiliary mods for MoreInterfaces. When MoreInterfaces is updated, all supported versions of MoreInterfaces will be updated simultaneously. Your auxiliary mod may encounter unpredictable issues due to internal API changes during this process. **If you have auxiliary requirements, please submit a code merge request directly.**

## Usage

Optional for clients / Optional for servers  
Simply place it in the `mod/plugin` folder according to the packaging type of MoreInterfaces.

## Configuration

MoreInterfaces will generate a configuration file named `moreinterfaces.json` in the `config` folder when the game/server starts.

- `enable`: Whether to enable MoreInterfaces' API service (even if you don't enable the API service, you can still use some internal features via commands).
- `port`: The API service port of MoreInterfaces.
- `tokens`: This is a list where each `token` object has two properties: a customizable `token` itself and the corresponding permission group for that `token`.
- `powers`: This is also a list where each `power` represents a permission group with a unique numeric `id` and a list of permissions (this list is meaningless for users with `0` level, who have access to all APIs).
- `aesKey`: Please do not modify this option arbitrarily; this entry is used for encrypted data transmission with remote packets.

**Note**: The permission list will ensure that there is always at least one `0` level (all permissions) permission group. Even if manually removed, it will be automatically generated again.

## Client Access to MoreInterfaces API

### Packet Structure

Network packets consist of the following parts:

1. **Module Identifier (MODS)**: Identifies the sending module `moreinterfaces`, length 14 bytes.
2. **Version Number (VERSION)**: Identifies the message version, length 5 bytes.
3. **Line Feed (\n)**: Separates the module identifier and version number, length 2 bytes.
4. **Serialization Type (type)**: Identifies the message serialization type, length 1 byte.
5. **Reserved Byte (0xff)**: Reserved, length 1 byte.
6. **Message Type (type)**: Identifies the message type, length 4 bytes.
7. **Message Length (length)**: Indicates the length of the message content, length 4 bytes.
8. **Message Content (content)**: The actual content of the message.

```
+----------------+----------------+----------------+----------------+----------------+----------------+----------------+
|    MODS        |    VERSION     |    \n          |    type        |    0xff        |    type        |    length      |
+----------------+----------------+----------------+----------------+----------------+----------------+----------------+
|    content     |                |                |                |                |                |                |
+----------------+----------------+----------------+----------------+----------------+----------------+----------------+
```

### Serialization Types

The serialization type identifies the message serialization method. The following types are currently supported:

- **JSON (byte:1)**: Serializes in JSON format, this will be in plain text.
- **AES_JSON (byte:2)**: Serializes in JSON format while using more secure network encryption.

### Message Types

Message types identify the specific type of message and can be retrieved using the `MessageFactory.valueOf` method to get the corresponding `Message` class.

### Exception Handling

If an exception occurs during decoding, the `exceptionCaught` method will be invoked for processing. If the exception is a `JsonSyntaxException`, a `NotSessionErrorMessage` will be sent, and the stack trace will be printed.

### Special Notes

The serialization methods used by the server and client are dynamic. If at any point the client returns a packet of type `AES_JSON`, all subsequent packets sent by the server will also be of type `AES_JSON`.
