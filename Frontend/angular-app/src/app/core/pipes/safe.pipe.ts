import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml, SafeResourceUrl, SafeScript, SafeStyle, SafeUrl } from '@angular/platform-browser';

/**
 * SafePipe - Pipe for bypassing Angular's built-in sanitization
 * 
 * ⚠️ SECURITY WARNING:
 * This pipe bypasses Angular's built-in XSS protection. Only use it with:
 * 1. Content from trusted sources (your own backend, not user input)
 * 2. Content that has been sanitized server-side
 * 3. Static content that you control
 * 
 * NEVER use this pipe with:
 * - User-generated content
 * - Content from external APIs
 * - Any untrusted source
 * 
 * For user-generated content, use Angular's default sanitization or
 * sanitize the content server-side before displaying it.
 */
@Pipe({
  name: 'safe',
  standalone: true
})
export class SafePipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) {}

  /**
   * Transform method that bypasses Angular's security for specific content types
   * 
   * @param value - The content to bypass sanitization for (MUST be from a trusted source)
   * @param type - The type of content: 'html', 'style', 'script', 'url', 'resourceUrl'
   * @returns Sanitized content that bypasses Angular's XSS protection
   * 
   * @security This method disables XSS protection. Ensure the input is from a trusted source.
   */
  transform(value: string, type: string): SafeHtml | SafeStyle | SafeScript | SafeUrl | SafeResourceUrl {
    // Validate input to prevent null/undefined issues
    if (!value) {
      return this.sanitizer.bypassSecurityTrustHtml('');
    }

    switch (type) {
      case 'html':
        // Only use for trusted HTML content (e.g., from your CMS, not user input)
        return this.sanitizer.bypassSecurityTrustHtml(value);
      case 'style':
        // Only use for trusted CSS styles
        return this.sanitizer.bypassSecurityTrustStyle(value);
      case 'script':
        // ⚠️ CRITICAL: Only use for scripts you control. Never for user input.
        return this.sanitizer.bypassSecurityTrustScript(value);
      case 'url':
        // Only use for URLs from trusted sources
        return this.sanitizer.bypassSecurityTrustUrl(value);
      case 'resourceUrl':
        // Only use for resource URLs (iframes, etc.) from trusted sources
        return this.sanitizer.bypassSecurityTrustResourceUrl(value);
      default:
        // Default to HTML sanitization bypass
        return this.sanitizer.bypassSecurityTrustHtml(value);
    }
  }
}
