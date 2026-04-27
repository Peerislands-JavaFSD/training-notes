# React + TypeScript Form Handling

### Why This Stack? (Quick Context)
- **React Hook Form**: Handles forms without cluttering your state. It's "uncontrolled" (DOM manages values), so it's fast and simple.
- **Zod**: Validates data *and* generates TypeScript types automatically. One schema = validation + types = no bugs.
- **Result**: Clean, type-safe forms that scale. Used by pros at companies like Vercel.

Install: `npm install react-hook-form zod @hookform/resolvers`

---

### Step 1: The Zod Schema – Your Form's Blueprint

**Core Code Snippet** (save as `schema.ts`):
```ts
import { z } from "zod";

export const formSchema = z.object({
  name: z.string().min(2, "Name too short"),
  email: z.string().email("Invalid email"),
  password: z.string().min(8, "Password too short"),
  terms: z.boolean(), // true/false
}).refine((data) => data.password.length > 6, {
  message: "Password needs uppercase + number",
  path: ["password"],
});

export type FormData = z.infer<typeof formSchema>;
```

**Detailed Breakdown**:
- `z.object({ ... })`: Defines your form fields like a TypeScript interface, but with built-in validation. Each field is a "refinement" chain.
    - `name: z.string().min(2, "Name too short")`: Expects a string. If shorter than 2 chars, shows custom error. (`.trim()` could add auto-whitespace cleanup.)
    - `email: z.string().email("Invalid email")`: Validates email format (e.g., blocks "fake"). Zod uses regex under the hood.
    - `password: z.string().min(8, "Password too short")`: Basic length check.
    - `terms: z.boolean()`: For checkboxes (true/false). We'll enforce `true` later.
- `.refine((data) => ..., { message, path })`: Cross-field magic! Here, it re-checks password for complexity. `path: ["password"]` attaches the error to the password field.
- `export type FormData = z.infer<typeof formSchema>;`: **Magic line!** Auto-generates exact TypeScript types from your schema. No more manual interfaces—your form data is now `{ name: string; email: string; ... }` everywhere.

**Why not manual validation?** Copy-pasting checks leads to bugs (e.g., forgetting to validate on submit). Zod is declarative: "This is valid if..."—easy to read/test.

---

### Step 2: The Form Component – Wiring It Up

**Core Code Snippet** (save as `MyForm.tsx`):
```tsx
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { formSchema, FormData } from "./schema";

export default function MyForm() {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = 
    useForm<FormData>({
      resolver: zodResolver(formSchema),
      mode: "onChange", // Validate as you type
    });

  const onSubmit = (data: FormData) => {
    console.log("Valid data:", data); // Or send to API
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input {...register("name")} placeholder="Name" />
      {errors.name && <span>{errors.name.message}</span>}

      <input {...register("email")} type="email" placeholder="Email" />
      {errors.email && <span>{errors.email.message}</span>}

      <input {...register("password")} type="password" placeholder="Password" />
      {errors.password && <span>{errors.password.message}</span>}

      <label>
        <input type="checkbox" {...register("terms")} />
        Accept terms
      </label>
      {errors.terms && <span>{errors.terms.message}</span>}

      <button type="submit" disabled={isSubmitting}>Submit</button>
    </form>
  );
}
```

**Detailed Breakdown** (Line-by-Line):
- `import { useForm } from "react-hook-form";`: The hook that does *everything*. No manual `useState` for each field!
- `import { zodResolver } from "@hookform/resolvers/zod";`: Bridge between Zod and the form. Tells RHF: "Use this schema to validate."
- `useForm<FormData>({ ... })`:
    - `<FormData>`: Types the entire form (from Zod). Autocomplete for fields? Yes! Errors? Typed!
    - `resolver: zodResolver(formSchema)`: "Validate using my schema." Runs Zod on changes/submit.
    - `mode: "onChange"`: Triggers validation *as you type* (real-time feedback). Options: `"onBlur"` (on leave field), `"onSubmit"` (only on submit).
- Destructuring:
    - `register`: Magic function! Call `register("name")` → gets `{ name: "name", onChange: ..., onBlur: ... }`. Hooks input to validation without state.
    - `handleSubmit`: Wrapper for your `onSubmit`. Runs validation first—if fails, stops. If passes, calls your function with clean `data: FormData`.
    - `formState: { errors, isSubmitting }`:
        - `errors`: Object like `{ name: { message: "Name too short" } }`. Typed! Access `errors.name?.message`.
        - `isSubmitting`: Boolean—true during async submit (e.g., API call). Use to disable button/show spinner.
- `const onSubmit = (data: FormData) => { ... }`:
    - Runs *only* on valid data. `data` is fully typed (e.g., `data.name` is string, guaranteed valid).
    - Add async: `const onSubmit = async (data) => { await api.post(data); }`. `isSubmitting` handles loading.
- JSX:
    - `<input {...register("name")} />`: Spreads props. Input now auto-validates + updates form state invisibly.
    - `{errors.name && <span>{errors.name.message}</span>}`: Conditional error display. `?.` for safety (e.g., `errors.name?.message`).
    - Checkbox: Same `register("terms")`—Zod expects boolean.
    - `<form onSubmit={handleSubmit(onSubmit)}>`: `handleSubmit` prevents default + validates.
    - `disabled={isSubmitting}`: UX win—blocks double-submits.

**What happens under the hood?**
1. User types in name → `onChange` from `register` triggers Zod.
2. Zod fails? → `errors.name` updates → re-render shows red text.
3. Submit? → `handleSubmit` runs full Zod check → if good, calls `onSubmit` with data.

**Common Pitfall**: For numbers, add `{ valueAsNumber: true }` to `register("age")`. Converts string → number for Zod.

---

### Step 3: Advanced Touches (With Explanations)

**Enforce Terms Checkbox** (add to schema):
```ts
terms: z.boolean().refine((val) => val === true, "Must accept terms"),
```
- `.refine()`: Custom check. Attaches error if false.

**Async Submit with Loading**:
```tsx
const onSubmit = async (data: FormData) => {
  isSubmitting starts true automatically during async.
  try {
    await fetch("/api/register", { method: "POST", body: JSON.stringify(data) });
    console.log("Success!");
  } catch (error) {
    // Manual error: setError("root", { message: "Server failed" });
  } finally {
    // isSubmitting goes false.
  }
};
```
- Explanation: RHF auto-manages `isSubmitting` for async functions. Add `setError` from `useForm` for API errors (shows at form top).

**Reset Form** (after success):
```tsx
const { ..., reset } = useForm(...);
onSubmit = async (data) => { ...; reset(); }; // Clears all fields
```
- `reset()`: Back to `defaultValues` (add them in `useForm` for pre-fills).

**Why Uncontrolled?** No `value={state}` + `onChange={setState}` per input. Less code, fewer re-renders, same power.

---

### Troubleshooting & Pro Tips
- **No errors showing?** Check `mode: "onChange"`—default is `"onSubmit"`.
- **Type errors?** Ensure `<FormData>` generic—VS Code will yell if mismatched.
- **Custom messages**: All in schema. User sees "Name too short", not "min(2)".
- **Testing**: `screen.getByRole("textbox", { name: /name/i })` in Jest—RHF plays nice.
- **Scale up**: For 50 fields? Same code. Add `watch("field")` to react to changes (e.g., show/hide based on selection).
