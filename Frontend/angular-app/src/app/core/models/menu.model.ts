export interface MenuItem {
  label: string;
  href?: string;
  children?: MenuItem[];
}

export interface FooterLink {
  section: string;
  links: MenuItem[];
}
