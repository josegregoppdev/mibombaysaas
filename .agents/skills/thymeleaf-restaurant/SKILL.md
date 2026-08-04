---
name: thymeleaf-restaurant
description: Restaurant SaaS UI with Spring Boot + Thymeleaf, HTMX, Alpine.js, and mobile-first responsive design
---

# Thymeleaf Restaurant SaaS UI

You are building a Restaurant SaaS using Spring Boot + Thymeleaf.

## Core Technology Stack
- **Backend:** Spring Boot (Java)
- **Templating:** Thymeleaf (Server-Side Rendering)
- **Interactivity:** HTMX (for partial page updates without full reload)
- **UI State:** Alpine.js (for modals, dropdowns, toggles, client-side state)
- **Styling:** Tailwind CSS (or Bootstrap if specified)

## RESPONSIVE STRATEGY (CRITICAL)
- **MOBILE-FIRST:** Always design for small screens (320px - 480px) FIRST.
- **Breakpoints:**
  - Mobile (< 640px): Full-width layouts, stacked cards, bottom-sheet modals
  - Tablet (640px - 1024px): Two-column grids, side-by-side order lists
  - Desktop (> 1024px): Full dashboard with sidebar and inline edit panels
- **Touch Targets:** All interactive elements MUST be at least 44px tall on mobile.

## Thymeleaf-Specific Patterns
- Use `th:fragment` for reusable components (menu-card, order-ticket, table-grid)
- Use `th:each` for iterating menu items and order lists
- Use `th:classappend` to dynamically add responsive classes
- Use `th:if` and `th:unless` for conditional rendering
- Always use `@{...}` for URL linking in forms and buttons

## HTMX Patterns (for interactivity)
- Use `hx-post` or `hx-get` for form submissions
- Use `hx-target` to specify where response HTML should be inserted
- Use `hx-swap` to control how content is swapped (outerHTML, innerHTML, etc.)
- Use `hx-trigger` for events (click, change, submit, etc.)
- Common patterns:
  - Adding item to cart: `hx-post="/cart/add" hx-target="#cart-count" hx-swap="innerHTML"`
  - Updating order status: `hx-post="/order/update" hx-target="#order-{id}" hx-swap="outerHTML"`
  - Kitchen display refresh: `hx-get="/kitchen/orders" hx-trigger="every 5s"`

## Alpine.js Patterns (for UI state)
- Use `x-data` to initialize component state
- Use `x-show` for conditional visibility (modals, dropdowns)
- Use `x-on` for event handling (click, change, etc.)
- Use `x-bind` for dynamic attributes
- Use `x-transition` for smooth animations
- Common patterns:
  - Mobile sidebar toggle: `x-data="{ open: false }" x-on:click="open = !open"`
  - Dropdown menu: `x-data="{ open: false }" x-on:click.outside="open = false"`

## Restaurant-Specific UI Patterns
- **Order Status Colors:**
  - PENDING: Yellow/Amber (`bg-yellow-100 text-yellow-800`)
  - COOKING: Orange (`bg-orange-100 text-orange-800`)
  - READY: Green (`bg-green-100 text-green-800`)
  - SERVED: Gray (`bg-gray-100 text-gray-800`)
  - CANCELLED: Red (`bg-red-100 text-red-800`)
- **Table Map:**
  - Free tables: Green border (`border-green-500`)
  - Occupied tables: Red border (`border-red-500`)
  - Reserved tables: Blue border (`border-blue-500`)
  - Grid layout using `grid-cols-3 md:grid-cols-4 lg:grid-cols-6`
- **POS Number Keypad:**
  - Standard 3x4 grid layout
  - Numbers 1-9, 0, and special buttons (Clear, Send to Kitchen)
  - Large buttons (minimum 56px height on mobile)
- **Menu Display:**
  - Mobile: Full-width cards with image, name, price, add button
  - Desktop: Grid layout (`grid-cols-2 lg:grid-cols-3`) with hover effects
  - Category filters using HTMX or Alpine.js tabs

## Accessibility Requirements
- All images must have `alt` attributes
- All form inputs must have associated `<label>` elements
- Color is not the only indicator of status (add icons or text labels)
- Keyboard navigation must work for all interactive elements
- Proper ARIA attributes for dynamic content (modals, alerts, live regions)

## Performance Considerations
- Lazy load images using `loading="lazy"`
- Use `th:src` with `@{...}` for proper asset versioning
- Minimize JavaScript by using HTMX for server-side interactions
- Use Alpine.js sparingly for lightweight UI interactions only
- Optimize Thymeleaf fragments to avoid duplication
