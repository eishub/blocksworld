# The Blocks World

<img align="right" src="https://github.com/eishub/blocksworld/wiki/blocksworld.png"/>

The Blocks World is a classic and famous toy domain in Artificial Intelligence.

Starting from an initial configuration of blocks the aim is to move blocks to a new goal configuration. Blocks are instantaneously moved by means of a virtual gripper. A block can only be moved if there is no other block sitting on top of it. A block can always be moved to the table.

## Releases

Releases can be found [here](https://github.com/eishub/blocksworld/releases) and include the Blocks World environment, a random generator for Block's World configurations, and a manual for the environment.

Releases can also be found in eishub's maven repository  [here](https://github.com/eishub/mvn-repo/tree/master/eishub/blocksworld).

Dependency information 
=====================

```
<repository>
  <id>eishub-mvn-repo</id>
  <url>https://raw.github.com/eishub/mvn-repo/master</url>
</repository>
```
	
```	
<dependency>
  <groupId>eishub</groupId>
  <artifactId>blocksworld</artifactId>
  <version>1.3.0</version>
</dependency>
```
