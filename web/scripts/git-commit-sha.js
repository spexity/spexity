import child from "child_process"

export const getCommitSha = () => {
  let commitHash = process.env.GITHUB_SHA ?? ""
  try {
    commitHash = child.execSync("git rev-parse HEAD").toString().trim()
  } catch (err) {
    console.error("Failed to retrieve git commit hash")
    console.error("err.message", err.message)
    console.error("err.stderr", err.stderr?.toString())
    console.error("err.stdout", err.stdout?.toString())
  }
  return commitHash.substring(0, 8)
}
