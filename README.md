# LLM assisted Mutation Tool

A tool that mutates Java source files using OpenAI GPT models and specific mutation operators. Useful for testing Java code resilience, analyzing LLM-assisted changes, and exploring mutation testing patterns.

---

## 📦 Requirements

- Java 21+
- Maven 3.5+
- OpenAI API Key (via `OPENAI_API_KEY` env variable)

---

## ⚙️ Setup

Clone the repo and run:

```bash
mvn clean compile
```

---

## 🛠️ Running the Tool

1. **For Single File Mutation:**

You can mutate a single Java file using the following command:

```
  mvn exec:java -Dexec.args="-f /path/to/java/file.java"
```


2. **For Mutating Multiple Files in a Directory:**

To mutate all Java files within a directory, run the following command:

```
  mvn exec:java -Dexec.args="-dir /path/to/java/file/directory"
```

**Note:** Currently, this feature is not available. If you try to run the command, you may encounter the following error: 

[ERROR] : No file(s) found in the specified directory...


---

## 🌍 Storing the OpenAI API Key

The OpenAI API Key is required to interact with OpenAI's GPT models.

### 1. On Windows:

To store the API key in your environment variables, follow these steps:

- Search for **Environment Variables** in the Windows search bar.
- Click **Edit the system environment variables**.
- Under the **System Properties** window, click on **Environment Variables**.
- In the **System variables** section, click **New**.
- Set the **Variable Name** to `OPENAI_API_KEY` and the **Variable Value** to your OpenAI API key.
- Click **OK** and restart your terminal to apply the changes.

### 2. On macOS:

- Open your terminal.
- Use the following command to open the `.bash_profile` (or `.zshrc` for newer macOS versions using Zsh):

  ```
  nano ~/.bash_profile
  ```

  or for Zsh users:

  ```
  nano ~/.zshrc
  ```

- Add the following line to the file:

  ```
  export OPENAI_API_KEY="your_openai_api_key"
  ```

- Save and exit by pressing `Ctrl + X`, then `Y`, and `Enter`.
- Apply the changes by running:

  ```
  source ~/.bash_profile
  ```

  or for Zsh users:

  ```
  source ~/.zshrc
  ```

Your API key will now be available as an environment variable.

---


